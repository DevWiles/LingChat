package org.lingchat.lingchatcommon.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class IdGenerator {

    /**
     * Id 生成器,在分布式系统中生成唯一ID。
     */

    private static final long EPOCH = 1483228800000L;  // 2017-01-01 00:00:00 UTC

    private static final long MACHINE_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS;

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long machineId = 1L;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    static {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac != null) {
                machineId = (((mac[mac.length - 1] & 0xFF) << 8) | (mac[mac.length - 2] & 0xFF)) & 0x3FF;
            }
        } catch (Exception e) {
            machineId = ManagementFactory.getRuntimeMXBean().getName().hashCode() & 0x3FF;
        }
    }

    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private IdGenerator() {
        throw new IllegalStateException("Utility class");
    }
}
