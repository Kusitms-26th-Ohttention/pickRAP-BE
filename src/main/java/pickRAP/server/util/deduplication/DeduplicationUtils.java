package pickRAP.server.util.deduplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeduplicationUtils {
    //Object 중복 제거
    public static <T> List<T> deduplication(final List<T> list, Function<? super T,?> key){
        return list.stream()
                .filter(deduplication(key))
                .collect(Collectors.toList());
    }

   private static <T> Predicate<T> deduplication(Function<? super T,?>key) {
        final Set<Object> set = ConcurrentHashMap.newKeySet();
        return predicate -> set.add(key.apply(predicate));
    }

    // String 중복 제거
    public static List<String> deduplication(List<String> list){
        return new ArrayList<String>(
                new HashSet<String>(list));
    }
}
