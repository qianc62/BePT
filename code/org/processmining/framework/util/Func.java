package org.processmining.framework.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Func {

	public interface Fun1<R, A> {
		R apply(A a);
	}

	public interface Fun2<R, A1, A2> {
		R apply(A1 a1, A2 a2);
	}

	public static <T1, T2> void map(Iterable<T1> seq, Fun1<T2, T1> fun,
			Collection<T2> dest) {
		for (T1 t : seq) {
			dest.add(fun.apply(t));
		}
	}

	public static <R, T> List<R> map(Iterable<T> seq, Fun1<R, T> fun) {
		List<R> result = new ArrayList<R>();
		for (T t : seq) {
			result.add(fun.apply(t));
		}
		return result;
	}

	public static <T> T accumulate(Iterable<T> seq, T start, Fun2<T, T, T> fun) {
		T accum = start;
		for (T t : seq) {
			accum = fun.apply(accum, t);
		}
		return accum;
	}

	public static String join(Iterable<String> seq, String separator) {
		return accumulate(seq, "", new Fun2<String, String, String>() {
			public String apply(String a, String b) {
				return a.length() == 0 ? b : a + "\\n" + b;
			}
		});
	}

	public static String joinAtMost(int maxnum, Iterable<String> sequence,
			String separator, String whenExceeded) {
		StringBuffer result = new StringBuffer();
		int i = 0;
		int hasMore = 0;

		for (String s : sequence) {
			if (i >= maxnum) {
				hasMore++;
			} else {
				if (i > 0) {
					result.append(separator);
				}
				result.append(s);
			}
			i++;
		}
		if (hasMore > 0 && whenExceeded != null) {
			result.append(separator);
			result.append(whenExceeded);
		}
		return result.toString();
	}

	public static String joinAtMost(int maxnum, Iterable<String> sequence,
			String separator, String beforeExceeded, String afterExceeded) {
		StringBuffer result = new StringBuffer();
		int i = 0;
		int hasMore = 0;

		for (String s : sequence) {
			if (i >= maxnum) {
				hasMore++;
			} else {
				if (i > 0) {
					result.append(separator);
				}
				result.append(s);
			}
			i++;
		}
		if (hasMore > 0 && beforeExceeded != null) {
			result.append(separator);
			result.append(beforeExceeded);
			if (afterExceeded != null) {
				result.append("" + hasMore);
				result.append(afterExceeded);
			}
		}
		return result.toString();
	}
}
