package com.ibm.streamsx.objectstorage.writer.parquet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.parquet.Preconditions;
import org.apache.parquet.io.api.Binary;
import org.joda.time.DateTimeUtils;

public class ParquetTimestamp {

	private static final long NANOS_PER_HOUR = TimeUnit.HOURS.toNanos(1);
	private static final long NANOS_PER_MINUTE = TimeUnit.MINUTES.toNanos(1);
	private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);

	private static final ThreadLocal<Calendar> fCalendar = new ThreadLocal<Calendar>();

    private static Calendar getCalendar(ThreadLocal<Calendar> cal, boolean useLocal) {
        if (cal.get() == null) {
            cal.set(useLocal ? Calendar.getInstance() : Calendar.getInstance(TimeZone.getTimeZone("GMT")));
        }
        return cal.get();
    }
    private static Calendar getCalendar(boolean useLocal) {
        Calendar calendar = getCalendar(fCalendar, useLocal);
        calendar.clear(); 
        
        return calendar;
    }

	public static Binary getBinary(Timestamp ts, boolean useLocal) {

		Calendar calendar = getCalendar(useLocal);
		calendar.setTime(ts);

		long timeOfDayNanos = ts.getNanos() 
				+ NANOS_PER_SECOND * calendar.get(Calendar.SECOND)
				+ NANOS_PER_MINUTE * calendar.get(Calendar.MINUTE)
				+ NANOS_PER_HOUR * calendar.get(Calendar.HOUR_OF_DAY);

		int julianDay = (int) DateTimeUtils.toJulianDay(calendar.getTimeInMillis());

		ByteBuffer buf = ByteBuffer.allocate(12);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putLong(timeOfDayNanos);
		buf.putInt(julianDay);
		buf.flip();
		
		return Binary.fromConstantByteBuffer(buf);
	}

//	public NanoTime fromBinary(Binary bytes) {
//		Preconditions.checkArgument(bytes.length() == 12, "Must be 12 bytes");
//		ByteBuffer buf = bytes.toByteBuffer();
//		buf.order(ByteOrder.LITTLE_ENDIAN);
//		this.timeOfDayNanos = buf.getLong();
//		this.julianDay = buf.getInt();
//		return new NanoTime(julianDay, timeOfDayNanos);
//	}

}
