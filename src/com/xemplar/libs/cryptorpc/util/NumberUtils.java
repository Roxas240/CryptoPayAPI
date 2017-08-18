package com.xemplar.libs.cryptorpc.util;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import com.neemre.btcdcli4j.core.common.Defaults;

public final class NumberUtils {
	
	public static boolean isEven(int integer) {
		return integer % 2 == 0;
	}

	public static <T> Map<T, BigDecimal> setValueScale(Map<T, BigDecimal> pairs, int newScale) {
		Iterator<Map.Entry<T, BigDecimal>> iterator = pairs.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<T, BigDecimal> pair = iterator.next();
			pair.setValue(pair.getValue().setScale(newScale, Defaults.ROUNDING_MODE));
		}
		return pairs;
	}
}