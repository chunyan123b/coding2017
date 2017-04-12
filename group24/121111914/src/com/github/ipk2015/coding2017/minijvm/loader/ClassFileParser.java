package com.github.ipk2015.coding2017.minijvm.loader;



import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.github.ipk2015.coding2017.minijvm.clz.AccessFlag;
import com.github.ipk2015.coding2017.minijvm.clz.ClassFile;
import com.github.ipk2015.coding2017.minijvm.clz.ClassIndex;
import com.github.ipk2015.coding2017.minijvm.constant.ClassInfo;
import com.github.ipk2015.coding2017.minijvm.constant.ConstantInfo;
import com.github.ipk2015.coding2017.minijvm.constant.ConstantPool;
import com.github.ipk2015.coding2017.minijvm.constant.FieldRefInfo;
import com.github.ipk2015.coding2017.minijvm.constant.MethodRefInfo;
import com.github.ipk2015.coding2017.minijvm.constant.NameAndTypeInfo;
import com.github.ipk2015.coding2017.minijvm.constant.NullConstantInfo;
import com.github.ipk2015.coding2017.minijvm.constant.StringInfo;
import com.github.ipk2015.coding2017.minijvm.constant.UTF8Info;
import com.github.ipk2015.coding2017.minijvm.field.Field;
import com.github.ipk2015.coding2017.minijvm.method.Method;
import com.github.ipk2015.coding2017.minijvm.util.Util;



public class ClassFileParser {

	public ClassFile parse(byte[] codes) {
		ClassFile classFile = new ClassFile();
		ByteCodeIterator iterator = new ByteCodeIterator(codes);
		
		String magicNum = iterator.nextUNToHexString(4);
		if(!"cafebabe".equalsIgnoreCase(magicNum)){
			return null;
		}
		
		classFile.setMinorVersion(iterator.nextUNToInt(2));
		classFile.setMajorVersion(iterator.nextUNToInt(2));
		
		ConstantPool constantPool = parseConstantPool(iterator);
		classFile.setConstPool(constantPool);
		
		AccessFlag accessFlag = parseAccessFlag(iterator);
		classFile.setAccessFlag(accessFlag);
		
		ClassIndex classIndex = parseClassInfex(iterator);
		classFile.setClassIndex(classIndex);
		
		parseInterfaces(iterator);
		
		parseFields(classFile,constantPool,iterator);
		
		parseMethods(classFile,iterator);
		
		return classFile;
	}

	private AccessFlag parseAccessFlag(ByteCodeIterator iter) {
		AccessFlag flag = new AccessFlag(iter.nextUNToInt(2));
		return flag;
	}

	private ClassIndex parseClassInfex(ByteCodeIterator iter) {
		ClassIndex classIndex = new ClassIndex();
		classIndex.setThisClassIndex(iter.nextUNToInt(2));
		classIndex.setSuperClassIndex(iter.nextUNToInt(2));
		return classIndex;

	}

	private ConstantPool parseConstantPool(ByteCodeIterator iter) {
		
		int poolSize = iter.nextUNToInt(2);
		
		ConstantPool pool = new ConstantPool();
		pool.addConstantInfo(new NullConstantInfo());
		
		int tag;
		for(int i = 1;i < poolSize; i++){
			tag = iter.nextUNToInt(1);
			switch(tag){
				case ConstantInfo.CLASS_INFO:
					meetClassInfo(pool,iter);
					break;
				case ConstantInfo.FIELD_INFO:
					meetFieldInfo(pool,iter);
					break;
				case ConstantInfo.METHOD_INFO:
					meetMethodInfo(pool,iter);
					break;
				case ConstantInfo.NAME_AND_TYPE_INFO:
					meetNameAndTypeInfo(pool,iter);
					break;
				case ConstantInfo.STRING_INFO:
					meetStringInfo(pool,iter);
					break;
				case ConstantInfo.UTF8_INFO:
					meetUTF8Info(pool,iter);
					break;
				default:
					throw new RuntimeException("还没有关于此的处理，tag:"+tag);
			}	
		}
		return pool;
	}
	
	private void meetClassInfo(ConstantPool pool,ByteCodeIterator iter){
		ClassInfo info = new ClassInfo(pool);
		info.setUtf8Index(iter.nextUNToInt(2));
		pool.addConstantInfo(info);
	}
	
	private void meetFieldInfo(ConstantPool pool,ByteCodeIterator iter){
		FieldRefInfo info = new FieldRefInfo(pool);
		info.setClassInfoIndex(iter.nextUNToInt(2));
		info.setNameAndTypeIndex(iter.nextUNToInt(2));
		pool.addConstantInfo(info);
	}
	
	private void meetMethodInfo(ConstantPool pool,ByteCodeIterator iter){
		MethodRefInfo info = new MethodRefInfo(pool);
		info.setClassInfoIndex(iter.nextUNToInt(2));
		info.setNameAndTypeIndex(iter.nextUNToInt(2));
		pool.addConstantInfo(info);
	}
	
	private void meetNameAndTypeInfo(ConstantPool pool,ByteCodeIterator iter){
		NameAndTypeInfo info = new NameAndTypeInfo(pool);
		info.setIndex1(iter.nextUNToInt(2));
		info.setIndex2(iter.nextUNToInt(2));
		pool.addConstantInfo(info);
	}
	
	private void meetStringInfo(ConstantPool pool,ByteCodeIterator iter){
		StringInfo info = new StringInfo(pool);
		info.setIndex(iter.nextUNToInt(2));
		pool.addConstantInfo(info);
	}
	
	private void meetUTF8Info(ConstantPool pool,ByteCodeIterator iter){
		int length = iter.nextUNToInt(2);
		byte[] data = iter.nextUNToArray(length);
		String value = null;
		try {
			value=new String(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		UTF8Info info = new UTF8Info(pool);
		info.setLength(length);
		info.setValue(value);
		pool.addConstantInfo(info);
	}
	
	private void parseInterfaces(ByteCodeIterator iter) {
		int interfaceCount = iter.nextUNToInt(2);

		System.out.println("interfaceCount:" + interfaceCount);

		// TODO : 如果实现了interface, 这里需要解析
	}

	private void parseFields(ClassFile classFile,ConstantPool pool,ByteCodeIterator iter){
		int count = iter.nextUNToInt(2);
		for(int i = 0;i < count;i++){
			classFile.addField(Field.parse(pool, iter));
		}
	}
	
	private void parseMethods(ClassFile classFile,ByteCodeIterator iter){
		int count = iter.nextUNToInt(2);
		for(int i = 0;i < count;i++){
			classFile.addMethod(Method.parse(classFile, iter));
		}
	}
	
}
