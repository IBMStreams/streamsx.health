package com.ibm.streamsx.objectstorage.internal.sink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.ibm.streams.operator.Tuple;


public class OSObjectSerializer implements Serializer<OSObject> {

	private static final Kryo kryo = new Kryo();

	public OSObjectSerializer(ClassLoader loader) throws ClassNotFoundException {
		kryo.register(OSObject.class);
		kryo.register(ArrayList.class);
		kryo.register(Tuple.class, new JavaSerializer());
		Class<?> clazz = Class.forName("com.ibm.streams.operator.internal.object.OpInputTuple");
		kryo.register(clazz, new JavaSerializer());		
	}

	@Override
	public boolean equals(OSObject osObject, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
		return osObject.equals(read(binary));

	}

	@Override
	public OSObject read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
		Input input = new Input(new ByteBufferInputStream(binary));
		return (OSObject)kryo.readClassAndObject(input);
	}

	@Override
	public ByteBuffer serialize(OSObject osObject) throws SerializerException {
		Output output = new Output(new ByteArrayOutputStream());
		kryo.writeClassAndObject(output, osObject);
		output.close();

		return ByteBuffer.wrap(output.getBuffer());
	}

}
