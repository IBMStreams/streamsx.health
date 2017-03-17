package com.ibm.streamsx.health.analyze.ecg;

import java.util.HashMap;
import java.util.Map;

public enum BeatType {

	NOTQRS(0),
	NORMAL(1),
	LBBB(2),
	RBBB(3),
	ABERR(4),
	PVC(5),
	FUSION(6),
	NPC(7),
	APC(8),
	SVPB(9),
	VESC(10),
	NESC(11),
	PACE(12),
	UNKNOWN(13),
	NOISE(14),
	ARFCT(16),
	STCH(18),
	TCH(19),
	SYSTOLE(20),
	DIASTOLE(21),
	NOTE(22),
	MEASURE(23),
	BBB(25),
	PACESP(26),
	RHYTHM(28),
	LEARN(30),
	FLWAV(31),
	VFON(32),
	VFOFF(33),
	AESC(34),
	SVESC(35),
	NAPC(37),
	PFUS(38),
	PQ(39),
	JPT(40),
	RONT(41),
	ACMAX(49);

	private static final Map<Integer, BeatType> codeMap = new HashMap<Integer, BeatType>();
    static {
        for (BeatType e : BeatType.values()) {
            if (codeMap.put(e.getCode(), e) != null) {
                throw new IllegalArgumentException("Duplicate code: " + e.getCode());
            }
        }
    }
	
	public static BeatType getByCode(int code) {
		return codeMap.get(code);
	}
    
	private int code;
	private BeatType(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
