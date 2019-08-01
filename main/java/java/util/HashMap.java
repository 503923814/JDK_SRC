/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hash table based implementation of the <tt>Map</tt> interface.  This
 * implementation provides all of the optional map operations, and permits
 * <tt>null</tt> values and the <tt>null</tt> key.  (The <tt>HashMap</tt>
 * class is roughly equivalent to <tt>Hashtable</tt>, except that it is
 * unsynchronized and permits nulls.)  This class makes no guarantees as to
 * the order of the map; in particular, it does not guarantee that the order
 * will remain constant over time.
 *
 * <p>This implementation provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the hash function
 * disperses the elements properly among the buckets.  Iteration over
 * collection views requires time proportional to the "capacity" of the
 * <tt>HashMap</tt> instance (the number of buckets) plus its size (the number
 * of key-value mappings).  Thus, it's very important not to set the initial
 * capacity too high (or the load factor too low) if iteration performance is
 * important.
 *
 * <p>An instance of <tt>HashMap</tt> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the hash table is <i>rehashed</i> (that is, internal data
 * structures are rebuilt) so that the hash table has approximately twice the
 * number of buckets.
 *
 * <p>As a general rule, the default load factor (.75) offers a good
 * tradeoff between time and space costs.  Higher values decrease the
 * space overhead but increase the lookup cost (reflected in most of
 * the operations of the <tt>HashMap</tt> class, including
 * <tt>get</tt> and <tt>put</tt>).  The expected number of entries in
 * the map and its load factor should be taken into account when
 * setting its initial capacity, so as to minimize the number of
 * rehash operations.  If the initial capacity is greater than the
 * maximum number of entries divided by the load factor, no rehash
 * operations will ever occur.
 *
 * <p>If many mappings are to be stored in a <tt>HashMap</tt>
 * instance, creating it with a sufficiently large capacity will allow
 * the mappings to be stored more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.  Note that using
 * many keys with the same {@code hashCode()} is a sure way to slow
 * down performance of any hash table. To ameliorate impact, when keys
 * are {@link Comparable}, this class may use comparison order among
 * keys to help break ties.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a hash map concurrently, and at least one of
 * the threads modifies the map structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more mappings; merely changing the value
 * associated with a key that an instance already contains is not a
 * structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.
 *
 * If no such object exists, the map should be "wrapped" using the
 * {@link Collections#synchronizedMap Collections.synchronizedMap}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the map:<pre>
 *   Map m = Collections.synchronizedMap(new HashMap(...));</pre>
 *
 * <p>The iterators returned by all of this class's "collection view methods"
 * are <i>fail-fast</i>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> method, the iterator will throw a
 * {@link ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author  Doug Lea
 * @author  Josh Bloch
 * @author  Arthur van Hoff
 * @author  Neal Gafter
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     TreeMap
 * @see     Hashtable
 * @since   1.2
 */

/**
 * 1、为什么数组长度必须是2的幂次方？
 *      因为：(1)计算元素存放的位置是使用 hashCode & (table.length -1), 而不是取模。比如数组的长度为16, 那么hashCode & 1111(16 - 1)
 *            还是hashCode后面4位的值。如果不是2的幂次方，比如15，那么hashCode & 1110(15 - 1), 那hashCode后4位的前三位相同，最后
 *            一位不同，计算出的结果还是一样，那么元素将不能均匀分布；
 *            (2)扩容元素存放位置的判断方法为：if( (e.hash & oldCap) == 0 ), 如果数组的长度不是2的幂次方，那么数组长度的2进制表示方法
 *               将会有多个1，这样元素也不能均为分布到新的数组。
 *
 * 2、为什么数组扩容必须增加2倍？
 *   比如原来的长度是16，那么现在长度是32, 则 oldCap = 16，二进制数为：10000, 现在在遍历 oldTab[1]的元素，
 *   则用e.hash值第5位的值是0还是1来判断元素存储在 newTab[1] 还是 newTab[17]. 所以每次table增长必须是2倍，
 *   而不能是4倍或者8倍... 因为第5位非0即1，只有两种状态。这由它的扩容机制决定了
 *
 * 3、两种情况会使HashMap扩容
 *  (1) 存入的元素个数达到阈值；
 *  (2) 链表长度达到8，但是table数组的长度小于64，这种情况也会让HashTable扩容。
 *
 *  4、红黑树转成链表的情况：
 *   (1) 扩容后链表的长度小于等于UNTREEIFY_THRESHOLD(6)的时候，会把红黑树转成链表；
 *   (2) 删除元素时，如果(root == null || root.right == null || (rl = root.left) == null || rl.left == null) 为tre，
 *         (判断树的结构是在删除元素之前)，那么也会把红黑树转成链表。
 *
 *  5、扩容后链表长度一定会变短吗？
 *      不一定，链表长度可能不变。
 *
 *  6、链表长度达到8一定会转成红黑树吗？
 *      (1) 不一定，如果这时候table的长度小于64的话，会对table进行扩容，扩容后链表的长度不一定变短，但是此时链表并没有转成红黑树。
 *      (2) put()方法链表长度达到9才会转成红黑树，computeIfAbsent()链表长度达到8就转成红黑树
 *
 */

public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * Implementation notes.
     *
     * This map usually acts as a binned (bucketed) hash table, but
     * when bins get too large, they are transformed into bins of
     * TreeNodes, each structured similarly to those in
     * java.util.TreeMap.
     * 大多数方法都尝试使用普通的bin，但在适用时中继到TreeNode方法(只需检查节点的instanceof)。
     * Most methods try to use normal bins, but
     * relay to TreeNode methods when applicable (simply by checking
     * instanceof a node).
     * 树节点的存储箱可以像其他存储箱一样被遍历和使用，但是在过度填充时支持更快的查找
      * Bins of TreeNodes may be traversed and
     * used like any others, but additionally support faster lookup
     * when overpopulated.
     * 但是，由于正常使用的大多数箱子并没有过度填充，
     * 所以在表方法的过程中可能会延迟检查树箱子是否存在。
     * However, since the vast majority of bins in
     * normal use are not overpopulated, checking for existence of
     * tree bins may be delayed in the course of table methods.
     *
     * 树箱(即。，其元素都是treenode的箱子)主要由hashCode排序，但在tie的情况下，
     * 如果两个元素属于相同的“class C implementation Comparable<C>”，
     * 则键入它们的compareTo方法来排序。
     * Tree bins (i.e., bins whose elements are all TreeNodes) are
     * ordered primarily by hashCode, but in the case of ties, if two
     * elements are of the same "class C implements Comparable<C>",
     * type then their compareTo method is used for ordering.
     *
     * 我们通过反射保守地检查泛型类型来验证这一点——请参见comparableClassFor方法
     * (We conservatively check generic types via reflection to validate
     * this -- see method comparableClassFor).
     * 当键具有不同的哈希值或可排序值时，在提供最坏情况O(log n)操作时，
     * 树箱增加的复杂性是值得的，
     * The added complexity of tree bins is worthwhile in providing worst-case O(log n)
     * operations when keys either have distinct hashes or are
     * orderable,
     * 因此，在意外或恶意使用hashCode()方法返回分布很差的值时，(分布不均匀)
     * 以及在许多键共享一个hashCode的情况下(只要它们也是可比较的)，性能会优雅地下降。
     * Thus, performance degrades gracefully under
     * accidental or malicious usages in which hashCode() methods
     * return values that are poorly distributed, as well as those in
     * which many keys share a hashCode, so long as they are also
     * Comparable.
     * 如果这两种方法都不适用，与不采取预防措施相比，我们可能会浪费大约两倍的时间和空间。但是，
     * 唯一已知的案例来自于糟糕的用户编程实践，这些实践已经非常缓慢，以至于没有什么区别
     * (If neither of these apply, we may waste about a
     * factor of two in time and space compared to taking no
     * precautions. But the only known cases stem from poor user
     * programming practices that are already so slow that this makes
     * little difference.)
     *
     * 因为TreeNode的大小大约是普通节点的两倍，所以我们只在箱子中包含
     * 足够的节点以保证使用时才使用它们(请参阅TREEIFY_THRESHOLD
     * Because TreeNodes are about twice the size of regular nodes, we
     * use them only when bins contain enough nodes to warrant use
     * (see TREEIFY_THRESHOLD).
     * And when they become too small (due to
     * removal or resizing) they are converted back to plain bins.
     * 在使用分布良好的用户哈希码时，很少使用树箱。
     * In usages with well-distributed user hashCodes, tree bins are
     * rarely used.
     * 理想情况下，在随机哈希码下，bin中节点的频率遵循泊松分布，
     * 默认调整阈值为0.75，平均参数约为0.5，尽管由于调整粒度而存在较大的差异
      * Ideally, under random hashCodes, the frequency of
     * nodes in bins follows a Poisson distribution
     * (http://en.wikipedia.org/wiki/Poisson_distribution) with a
     * parameter of about 0.5 on average for the default resizing
     * threshold of 0.75, although with a large variance because of
     * resizing granularity.
     * Ignoring variance, the expected
     * occurrences of list size k are (exp(-0.5) * pow(0.5, k) /
     * factorial(k)). The first values are:
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: less than 1 in ten million
     *
     * 树状容器的根通常是它的第一个节点。但是，有时(目前仅在Iterator.remove之后)，
     * 根可能在其他地方，但是可以通过父链接(方法TreeNode.root())恢复。
     * The root of a tree bin is normally its first node.  However,
     * sometimes (currently only upon Iterator.remove), the root might
     * be elsewhere, but can be recovered following parent links
     * (method TreeNode.root()).
     *
     * 所有适用的内部方法都接受hashCodes作为参数(通常由公共方法提供)，允许它们在
     * 不重新计算用户hashCodes的情况下相互调用。大多数内部方法也接受“tab”参数，
     * 这通常是当前表，但在调整大小或转换时可能是新的或旧的。
     * All applicable internal methods accept a hash code as an
     * argument (as normally supplied from a public method), allowing
     * them to call each other without recomputing user hashCodes.
     * Most internal methods also accept a "tab" argument, that is
     * normally the current table, but may be a new or old one when
     * resizing or converting.
     *
     * 当bin列表被treeified、split或untreeified时，我们将它们保持相同的相对
     * 访问/遍历顺序(即为了更好地保存局部，并稍微简化对调用iterator.remove的分割
     * 和遍历的处理。当在插入时使用比较器时，为了保持跨重新平衡的总顺序
     * (或尽可能接近这里的要求)，我们将类和identityhashcode作为连接符进行
     * When bin lists are treeified, split, or untreeified, we keep
     * them in the same relative access/traversal order (i.e., field
     * Node.next) to better preserve locality, and to slightly
     * simplify handling of splits and traversals that invoke
     * iterator.remove. When using comparators on insertion, to keep a
     * total ordering (or as close as is required here) across
     * rebalancings, we compare classes and identityHashCodes as
     * tie-breakers.
     *
     * 由于LinkedHashMap子类的存在，普通vs树模式之间的使用和转换变得复杂。
     * 有关定义在插入、删除和访问时调用的钩子方法，请参见下面，
     * 这些方法允许LinkedHashMap内部保持独立于这些机制。
     * (这还要求将map实例传递给一些可能创建新节点的实用程序方法。)
     * The use and transitions among plain vs tree modes is
     * complicated by the existence of subclass LinkedHashMap. See
     * below for hook methods defined to be invoked upon insertion,
     * removal and access that allow LinkedHashMap internals to
     * otherwise remain independent of these mechanics. (This also
     * requires that a map instance be passed to some utility methods
     * that may create new nodes.)
     *
     * 基于并行编程的类似于ssa的编码风格有助于避免所有扭曲指针操作中的混叠错误。
     * The concurrent-programming-like SSA-based coding style helps
     * avoid aliasing errors amid all of the twisty pointer operations.
     */

    /**
     * 必须是2的幂次方
     * The default initial capacity - MUST be a power of two.
     */
    // 默认的初始容量： 2的4次方 = 16
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    // 最大容量: 2的30次方
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    // 默认负载因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 容器使用树来替换列表的节点个数阈(yu)值。当向至少具有这么多节点的容器添加元素时，
     * 容器将转换为树。该值必须大于2，并且应该至少为8，以便与树木移除中关于收缩后转换回
     * 普通垃圾箱的假设相吻合。
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2 and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     */
    // 转成红色树节点个数的阈值
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     */
    // 当桶(bucket)上的结点数小于这个值时树转链表
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 最小的 table 容量，其中的箱子可以treeified。(否则，如果一个bin中有太多节点，
     * 则会调整表的大小。)应至少为4 * TREEIFY_THRESHOLD，
     * 以避免调整大小和treeification阈值之间的冲突。
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
     * between resizing and treeification thresholds.
     */
    // 桶中结构转化为红黑树对应的table的最小容量（当table的容器小于这个值时，容器中节点
    // 个数太多只会把table的容量增大）
    // 至少为4 * TREEIFY_THRESHOLD
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        // key的哈希值
        final int hash;
        final K key;
        V value;
        // 下一个节点
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        // 这个hashCode()方法并没有在HashMap 中使用
        public final int hashCode() {
            // o != null ? o.hashCode() : 0;  ^ 异或
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                // 键值都相等时，返回true
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /* ---------------- Static utilities -------------- */

    /**
     * 计算key.hashCode()，并将(XORs)的散列值由高向低扩展
     * Computes key.hashCode() and spreads (XORs) higher bits of hash
     * to lower.
     * 由于该表使用了2的幂掩码，因此仅在当前掩码之上以位为单位变化的散列集总是会发生冲突。
     * (已知的例子中有一组浮点键，它们在小表中保存连续的整数)因此，我们应用了一种转换，将较高位的影响向下传播。
     * 位扩展的速度、实用性和质量之间存在权衡。因为许多常见的散列集已经合理分布(所以不要受益于传播),
     * 因为我们用树来处理容器中有大量的数据,我们只是XOR一些改变以最便宜的方式来减少系统lossage,以及将最高位的影响,
     * 否则永远不会因为指数计算中使用的表。
     * Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    static final int hash(Object key) {
        int h;
        // key的hash值高16位不变，低16位与高16位异或作为key的最终hash值。
        // h >>> 16，表示无符号右移16位，高位补0，任何数跟0异或都是其本身，因此key的hash值高16位不变

        // 这么做的原因是：元素存放位置的计算是 hashCode & (table.length - 1), 比如table的长度为16：则 hashCode & 01111(16 - 1)，
        // 只有hashCode的后面4位有用，其他位进行 & 运算之后都是0。所以这样做可以使元素分布更加均匀，
        // 用到了hashCode的第一个字节的后4位，和第三个字节的后4位
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    // 判断x的类是否是 "class C implements Comparable<C>" 类型的
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            // c = x.getClass(), 并判断c是否是 String.class
            if ((c = x.getClass()) == String.class) // bypass checks
                // 如果是字符串，则通过检查
                return c;

            // ts = c.getGenericInterfaces(), 并判断是否为null
            // 返回表示某些接口的 Type，这些接口由此对象所表示的类或接口直接实现。
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // type arg is c
                        // p.getActualTypeArguments() 获取泛型类型
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /**
     * Returns a power of two size for the given target capacity.
     */
    // 获取大于等于 cap的最小的2的幂的数， 若 cap = 001000000，这种类型的，也就是只有一个1，本来就是2的幂，那么就会相等
    static final int tableSizeFor(int cap) {
        // 减1的目的：cap -1 这样如果cap本身就是2的幂，那么就会获取到相等的数
        int n = cap - 1;
        // >>> 操作符表示无符号右移，高位取0; n >>> 1 并不会改变 n 的值
        // 若 n = 0100000
        n |= n >>> 1; // n = 0100000 | 0010000 = 0110000;  1、如果n只有一个1，赋值之后有两个1
        n |= n >>> 2; // n = 0110000 | 0001100 = 0111100;  2、4个1
        n |= n >>> 4; // n = 0111100 | 0000011 = 0111111;  3、8个1
        n |= n >>> 8; //                                   4、16个1
        n |= n >>> 16;//                                   3、32个1
        // -> 所以，一个Integer类型如果最高位是1的话，执行完成之后能把所以的位都变成1。
        //    也就是从1所在的最高位之后的所有位都会变成1,然后 +1之后就会变成2的幂的数

        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- Fields -------------- */

    /**
     * resized： 调整大小
     * The table, initialized on first use, and resized as
     * 长度总是2的幂(2的倍数)
     * necessary. When allocated, length is always a power of two.
     * (在某些操作中，我们还允许长度为零，以允许当前不需要的引导机制。)
     * (We also tolerate length zero in some operations to allow
     * bootstrapping mechanics that are currently not needed.)
     */
    // Node数组 -> 存储（位桶）的数组
    transient Node<K,V>[] table;

    /**
     * Holds cached entrySet(). Note that AbstractMap fields are used
     * for keySet() and values().
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * The number of key-value mappings contained in this map.
     */
    transient int size;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
    transient int modCount;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    // (The javadoc description is true upon serialization.
    // Additionally, if the table array has not been allocated, this
    // field holds the initial array capacity, or zero signifying
    // DEFAULT_INITIAL_CAPACITY.)

    // 阈值, 当实际大小(容量*填充因子)超过临界值时，会进行扩容
    // 阈值不一定的2的幂，最大为 Integer.MAX_VALUE
    // 构造方法中把它调整为2的幂的原因：当table的长度为0时，如果指定了threshold，那么会使用threshold作为table的长度
    // if the table array has not been allocated, this
    // field holds the initial array capacity, or zero signifying
    int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;

    /* ---------------- Public operations -------------- */

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    // 初始化时initialCapacity先保存在阈值字段里面。通过tableSizeFor() 方法转成大于等于 initialCapacity的最小的2的幂的数
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        // 初始容量大于MAXIMUM_CAPACITY时，设置为MAXIMUM_CAPACITY，而不报错
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        // tableSizeFor() 获取大于等于 initialCapacity的最小的2的幂的数
        // 把它调整为2的幂的原来是：当table的长度为0时，如果指定了threshold，那么会使用threshold作为table的长度
        // resize() 方法还会把 threshold 赋值为： threshold * 负载因子。
        // initialCapacity 实际上是指定 table的长度，而不是threshold的值，因为作为threshold时还会乘以负载因子。
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public HashMap() {
        // loadFactor 负载因子
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>.  The <tt>HashMap</tt> is created with
     * default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param   m the map whose mappings are to be placed in this map
     * @throws  NullPointerException if the specified map is null
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * Implements Map.putAll and Map constructor
     *
     * @param m the map
     * @param evict false when initially constructing this map, else
     * true (relayed to method afterNodeInsertion).
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // pre-size
                // size / loadFactory 然后再加1，这样就不会进行扩容了
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    // table == null, 这时候阈值其实是 table的长度.
                    // 如果已知需要的 table长度大于指定的 table长度，那么修改指定的table 长度
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                // 如果 table != null 且 size > threshold，先进行扩容(扩容后容量不一定够)
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    // 判断key是否存在的依据：key==null ? k==null : key.equals(k)
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * Implements Map.get and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @return the node, or null if none
     */
    final Node<K,V> getNode(int hash, Object key) {
        // n = tab.length, first 数组对应索引位置上的第一个节点
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            // 先检查第一个节点的元素，因为大多数情况下第一个节点的元素就是查找的元素了，
            // 没有找到再去判断是从红黑树中查找还是链表中查找
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) { // 判断是否还有下一个元素，没有就不用再查找了
                if (first instanceof TreeNode)
                    // 从红黑中查找
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        // containsKey() 跟 get(key) 方法一样，都是调用getNode() 方法，只是后者把value返回，前者判断是否存在
        return getNode(hash(key), key) != null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V put(K key, V value) {
        // 添加元素
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode. -> table处于创建模式
     * @return previous value, or null if none
     */
    // onlyIfAbsent = true 时，只有key不存在，才会进行插入
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        // n -> table数组的长度, i -> key对应元素存到数组的索引位置, p = tab[i]
        Node<K,V>[] tab; Node<K,V> p; int n, i;

        // table == null 或者长度为 0 时，调整table的大小
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;

        if ((p = tab[i = (n - 1) & hash]) == null)
            // 若元素存放的位置还没有存入元素
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            // p -> tab[i]
            // 先判断哈希值是否相等，相等再判断 p.key == key || key.equals(k)
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                // key 已经在map 中存在，下面会判断是否更新原来的值
                e = p;
            else if (p instanceof TreeNode)
                // 红黑树插入数据
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    // e = null，所以下面不会再更新元素的值
                    if ((e = p.next) == null) {
                        // 把新的元素插入进去，放到p的下一个节点位置
                        p.next = newNode(hash, key, value, null);
                        // todo 长度8会转成红黑树吗？
                        // TREEIFY_THRESHOLD = 8，链表长度达到9才转成红黑树，因为从0开始算，当binCount = 7的时候链表已经有8个元素了，
                        // 再加上新插入的一个，所以链表长度达到9才转成红黑树。computeIfAbsent()方法链表长度达到8就转成红黑树。
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            // 把链表转成红黑树
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 如果p.next 不为空，判断key是否相等，以判断key是否已存在map中
                    if (e.hash == hash &&
                            // 如果map中已经有null 键，那么 e.hash == hash && e.key == key (null == null)
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        // 已存在map中，结束循环，此时e不为null，下面会判断是否更新key映射的值
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                // onlyIfAbsent -> false
                if (!onlyIfAbsent || oldValue == null)
                    // 更新值
                    e.value = value;
                // hashMap中方法是空的，LinkedHashMap才会实现这个方法
                afterNodeAccess(e);
                // 返回原来的值，modCount不会增加 -> 也就是只是更新key 映射的值，modCount不会增加
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            // 先++ 再比较，所以size只要超过了阈值就会扩容
            resize();

        // hashMap中方法是空的，LinkedHashMap才会实现这个方法
        afterNodeInsertion(evict);

        // key 插入之前没有存入到map中，所以返回null
        return null;
    }

    /**
     * Initializes or doubles table size.  If null, allocates in
     * accord with initial capacity target held in field threshold.
     * Otherwise, because we are using power-of-two expansion, the
     * elements from each bin must either stay at same index, or move
     * with a power of two offset in the new table.
     *
     * @return the table
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        // oldCap table原来的长度
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;

        // 若oldCap 大于0
        if (oldCap > 0) {
            // 如果长度已经达到了2的30次方，那么长度没办法继续增加了(必须是2的幂, Integer的最大值是2的31次方 - 1)
            if (oldCap >= MAXIMUM_CAPACITY) {
                // 调整阈值，阈值最大可以设置到 Integer.MAX_VALUE
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }

            // newCap = oldCap << 1， 原来的长度的两倍
            // ~m1
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                // 阈值调整为原来的两倍
                newThr = oldThr << 1; // double threshold
        }

        // oldCap 的长度 <= 0，而oldThr > 0，这种情况下 oldThr是构造方法赋值的
        else if (oldThr > 0) // initial capacity was placed in threshold 初始容量设置为阈值(初始容量放置在阈值)
            // 初始容量设置为阈值，这就是为什么构造函数初始化阈值时需要把阈值调整为2的幂的原因
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            // 如果没有指定 threshold，那么table的长度使用默认值：16
            newCap = DEFAULT_INITIAL_CAPACITY;
            // 阈值 = (int) 0.75 * 16 = 12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }

        // 如果构造方法初始化了阈值(initialCapacity), 第一次调用resize()方法时newThr == 0, 那么阈值需要乘以负载因子
        // 或者上面的~m1条件不满足时，newThr = 0
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            // 构造方法可能传入大于 MAXIMUM_CAPACITY 的值 || 扩容后容量达到MAXIMUM_CAPACITY，那么设置阈值为 Integer.MAX_VALUE
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];   // 创建一个新的数组
        table = newTab;  // 设置 table = newTab
        if (oldTab != null) {
            // 原来table的数据需要重新计算其所在新的数组位置，然后添加到新的数组中
            for (int j = 0; j < oldCap; ++j) { // 这里 ++j 和 j++ 没有任何区别
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;  // 原来数组的元素值设置为null
                    // e 没有下一个节点
                    if (e.next == null)
                        // todo： 这时候 newTab[e.hash & (newCap - 1)] 一定没有元素吗？
                        // ==> 这时候 newTab[e.hash & (newCap - 1)] 一定没有元素元素。因为：如果原来的长度是16，那么现在长度是32，
                        // 那么原来 oldTab[1]的元素现在只会在 newTab[1] 或者newTab[17], oldTab[2]的元素现在只会在 newTab[2] 或者newTab[18],
                        // oldTab[3]还没遍历时，那么newTab[3] 和 newTab[19]的元素肯定都是空的
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        // 红黑树分割
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;

                            // 比如原来的长度是16，那么现在长度是32, 则 oldCap = 16，二进制数为：10000, 现在在遍历 oldTab[1]的元素，
                            // 则用e.hash值第5位的值是0还是1来判断元素存储在 newTab[1] 还是 newTab[17]. 所以每次table增长必须是2倍，
                            // 而不能是4倍或者8倍... 因为第5位非0即1，只有两种状态。这由它的扩容机制决定了
                            // --> 如果是对hash值取模的也可以实现，然后这里改成 if( e.hash % newCap < oldCap) ，但是效率没有这个高
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 比如j = 1，oldCap = 16，则 newTab[1] = loHead；newTab[17] = hiHead
                        if (loTail != null) {
                            // 注意： 这里必须设置 loTail.next = null，说明没有下一个node了，设置之前可能为非null,
                            // 比如： oldTable[1]有两个元素，现在分布到 newTable[1] 和 newTable[17]了
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            // 注意： 这里必须设置 hiTail.next = null，说明没有下一个node了，设置之前可能为非null
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        // 如果tab的长度 < 64，那么对tab进行扩容，不把链表转成红黑树
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            // e = tab[index] 第一个节点的位置
            // hd: header -> 第一个节点
            // tl: Last -> 上一个节点
            TreeNode<K,V> hd = null, tl = null;
            // 把index索引上的所有元素都连接成了TreeNode类型的双向链表
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
                // e = e.next
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V remove(Object key) {
        Node<K,V> e;
        // 删除元素
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    /**
     * Implements Map.remove and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to match if matchValue, else ignored
     * @param matchValue if true only remove if value is equal
     * @param movable if false do not move other nodes while removing 是否把根节点移到链表的第一个位置
     * @return the node, or null if none
     */
    // remove()方法 movable传入true, 迭代器删除的时候 movable传入false，
    // 迭代如果移动了元素，那么就可以导致原来根节点的元素没有被遍历到
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
                // p = tab[index]
            (p = tab[index = (n - 1) & hash]) != null) {
            // tab 有元素 且 key所在的索引位置元素不为空

            // node -> 删除的元素
            Node<K,V> node = null, e; K k; V v;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                // 找到删除的元素
                node = p;
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    // 从红黑树中查找元素
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            // 找到删除的元素，结束循环
                            node = e;
                            break;
                        }
                        p = e;
                        // 直到找到删除的元素 或者已经没有下一个元素了
                    } while ((e = e.next) != null);
                }
            }

            // matchValue = true 的时候，node.value需要和value相等才会删除元素
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);

                // node 不是TreeNode类型的，所以p == node 或者 p是 node的父节点
                else if (node == p)   // 当 node == p 时，说明删除的节点是第一个节点
                    // 第一节点指向 node.next，这样第一个节点就从链表中移除了
                    tab[index] = node.next;
                else
                    // p.next = node.next 这样node 就从链表中移除了
                    p.next = node.next;

                ++modCount;
                --size;

                // 空方法，LinkedHashMap 才会实现这个方法
                afterNodeRemoval(node);

                // 返回删除的节点
                return node;
            }
        }
        return null;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                // 不是把 table赋值为null, 而是把 table的每个元素都赋值为null
                tab[i] = null;
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                // 红黑树也维护了一个双向链表，所以可以这样查找
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    // == 可以判断 null 值
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        Set<K> ks;
        return (ks = keySet) == null ? (keySet = new KeySet()) : ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }

        // 调用iterator() 方法，新创建一个KeyIterator
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            // 每次调用方法的时候都会新创建一个
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        // 传入key值
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs;
        return (vs = values) == null ? (values = new Values()) : vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // Overrides of JDK8 Map extension methods

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        // 设置 onlyIfAbsent = ture,则 key不存在才会进插入
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        // 当前key存在，且映射的值和 oldValue相等，才会替换成新的值
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        // 当前key 存在就把原来的值替换成新的值
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    // 这个方法是插入到链表的第一个位置，put()方法是插入到链表的最后位置，红黑树插入则一样
    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;

        // binCount 计算链表的长度
        int binCount = 0;
        TreeNode<K,V> t = null;

        // old -> key 映射的值
        Node<K,V> old = null;

        // put()方法，插入完成后会 size > threshold 就会进行扩容，所以一般情况下不会存在 size > threshold
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            // 扩容
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                // getTreeNode()若没找到返回null
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;   // binCount -> key 在链表中的位置，若没找到则为链表的长度
                } while ((e = e.next) != null);
            }
            V oldValue;
            // 若使用key找到的node不为null，且它的值不为null，则返回原来的值
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }

        V v = mappingFunction.apply(key);
        if (v == null) {    // 若生成的值也是null, 则返回null
            return null;
        } else if (old != null) {
            // 若 old 不为null, old的值为null，则只要设置old的value为新的值即可
            old.value = v;
            afterNodeAccess(old);
            return v;
        }

        // -----------   插入新的元素   ------------

        // 若 first instanceof TreeNode，则 t = first，否则t 为null
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            // 插入到链表的第一个位置，put()方法是插入到链表的最后位置
            tab[i] = newNode(hash, key, v, first);
            // binCount >= TREEIFY_THRESHOLD - 1，则再插入一个元素之后，链表的长度至少为 TREEIFY_THRESHOLD，
            // 所以需要转成红黑树。链表长度达到8就转成红黑树。put()方法链表长度9才转成红黑树
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);

        // key 存在map中且映射的值不为null才会替换成新值
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {    // 若新值不为null，则进行替换
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            else  // 若新值为 null， 则直接移除 key
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    // 若key存在，若新的值不为null，则使用新的值替换原来的值，若新的值为null，则移除key;
    // 若key 不存在，则进行插入
    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);

        if (old != null) {
            if (v != null) { // 若 old 不为null，即 key在map中存在，且新的值不为null，则使用新的值替换原来的值
                old.value = v;
                afterNodeAccess(old);
            }
            else
                // 若新的值为null， 则移除 key
                removeNode(hash, key, null, false, true);
        }

        else if (v != null) {
            // 若 first instanceof TreeNode，则 t = first，否则t 为null
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);  // 插入到红黑树中
            else {
                tab[i] = newNode(hash, key, v, first); // 插入到链表的第一个位置
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);  // 链表长度达到8则转成红黑树
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }

        // 返回新的值
        return v;
    }

    // 1、若key存在，若key 映射的值不为null，则重新计算新的值，参数传入 (old.value, value); 若key 映射的为null，则使用value作为新的值，
    //     若新的值不为空，则替换原来的值，若新的值为null，则移除 key.
    // 2、 若key 不存在则插入新的值
    // 两种情况返回的都是新的值
    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();

        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)  // 若 old的值不为 null，则重新计算新的值
                v = remappingFunction.apply(old.value, value);
            else
                v = value; // 若old的值为null，则使用 value作为新的值
            if (v != null) {    // 若新的值不为空，则使用新的值替换原来的值
                old.value = v;
                afterNodeAccess(old);
            }
            else    // 若新的值为null，则移除 key
                removeNode(hash, key, null, false, true);
            return v;   // 返回新的值
        }

        // 若map 中不存在 key
        if (value != null) {
            if (t != null)  // 插入到红黑树中
                t.putTreeVal(this, tab, hash, key, value);
            else {
                // 插入到链表的第一个位置
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);  // 若链表长度达到 8则转成红黑树
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;   // 返回新插入的值
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            // 遍历所有的元素， accept()方法传入 key 和 value
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    // 值替换成新的值。 apply方法传入 key 和原来的 value
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // Cloning and serialization

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    // These methods are also used when serializing HashSets
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the
     *             bucket array) is emitted (int), followed by the
     *             <i>size</i> (an int, the number of key-value
     *             mappings), followed by the key (Object) and value (Object)
     *             for each key-value mapping.  The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                                             loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                             mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);
            @SuppressWarnings({"rawtypes","unchecked"})
                Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    /* ------------------------------------------------------------ */
    // iterators

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return
        Node<K,V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                // next 指向第一个元素
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            // current = e = next;      next = e.next
            if ((next = (current = e).next) == null && (t = table) != null) {
                // 如果next = null, 从数组的其他索引位置去查找
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            // 修改expectedModCount，防止抛出并发修改异常
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    /* ------------------------------------------------------------ */
    // spliterators

    static class HashMapSpliterator<K,V> {
        // this -> HashMap
        final HashMap<K,V> map;
        Node<K,V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index 最后一个索引
        int est;                    // size estimate  初始值为 0
        int expectedModCount;       // for comodification checks

        // HashMap.this, 0, -1, 0, 0
        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use 第一次使用的时候初始化fence 和 size
            int hi;
            if ((hi = fence) < 0) { // 构造方法传入 -1
                HashMap<K,V> m = map;
                est = m.size;   // set 初始化为 map.size()
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;  // fence = tab.length
            }
            // 返回的是 数组的长度
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<K> {

        // new KeySpliterator<>(HashMap.this, 0, -1, 0, 0)
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        // 每次调用spliterator()方法的时候，都会新创建一个KeySpliterator，
        // 所以遍历一次就没用了，因此不需要再遍历完成之后初始化index等值

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;  // mid 取中间值
            return (lo >= mid || current != null) ? null :
                    // HashMap<K,V> m, int origin, int fence, int est, int expectedModCount
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);    // 分割完成之后 index = mid，所以再遍历的时候就是从mid开始了
        }

        // 遍历方法
        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;

            // 正常流程是先调用 estimateSize()方法，estimateSize()方法会调用getFence()方法进行初始化
            if ((hi = fence) < 0) {  // hi = fence
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;

           // i = index 把index赋值给i, 然后 index = hi，因为遍历完成后 index = hi
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;   // 设置 current = null ，所以上面条件 current != null 是什么情景? -> tryAdvance()会设置current值，tryAdvance()
                do {              //  只遍历一个元素 ，如果这个元素已经在最后索引的位置，则此时 index = hi，但是可能还有元素，所以判断 current != null
                    if (p == null)
                        p = tab[i++]; // 该索引位置上的元素都遍历完成了 i 才会 ++
                    else {
                        // action传入的是wrappedSink, wrappedSink是第一个中间操作的sink，如果没有中间操作的sink，则为最后结束的sink
                        // sink的accept方法里面会继续调用下一个sink的 accept()方法
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi); // p.next != null || i < hi ，继续循环
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            // getFence()返回数组的长度
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next; // 设置current = current.next
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        // 只遍历一个元素，然后就返回了
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    // 逻辑跟KeySpliterator 一样的
    static final class ValueSpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    // 逻辑跟KeySpliterator 一样的
    static final class EntrySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap support


    /*
     * The following package-protected methods are designed to be
     * overridden by LinkedHashMap, but not by any other subclass.
     * Nearly all other internal methods are also package-protected
     * but are declared final, so can be used by LinkedHashMap, view
     * classes, and HashSet.
     */

    // Create a regular (non-tree) node
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // 把TreeNode 转成 Node
    // For conversion from TreeNodes to plain nodes
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 把Node转成TreeNode
    // For treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * Reset to initial default state. -----  Called by clone and readObject -----.
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     *  which in turn extends Node -> 从而扩展了Node
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * 因此可以作为普通节点或链接节点的扩展
     * extends Node) so can be used as extension of either regular or
     * linked node.
     */
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        // 上一个元素，与next相反
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * Returns root of tree containing this node.
         */
        final TreeNode<K,V> root() {
            // r = this
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    // 返回根节点
                    return r;
                r = p;
            }
        }

        /**
         * 确保根节点是在容器的第一个节点
         * Ensures that the given root is the first node of its bin.
         */
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                // 若tab 中有元素

                // 计算元素在tab 中的索引位置
                int index = (n - 1) & root.hash;

                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                // 如果root不是在tab上的第一个节点位置
                if (root != first) {
                    Node<K,V> rn;  // root.next
                    // 把root放到tab上的第一个节点位置
                    tab[index] = root;

                    // 把root放到链表的第一个位置，first放到 root 之后
                    // first -> rp -> root -> rn  ==> root -> first -> rp -> rn
                    // 如果first.next = root 也没有问题，因为这时候 rp 就是 first
                    TreeNode<K,V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    // root 已经在第一个节点位置，所以 prev 设置为 null
                    root.prev = null;
                }

                // 检查链表结构和红黑树结构是否正常

                // assert关键字是从JAVA SE 1.4 引入的，为了避免和老版本的Java代码中使用了assert关键字导致错误，
                // Java在执行的时候默认是不启动断言检查的（这个时候，所有的断言语句都 将忽略！），如果要开启断言检查，
                // 则需要用开关-enableassertions或-ea来开启
                assert checkInvariants(root);
            }
        }

        /**
         * Finds the node starting at root p with the given hash and key.
         * kc 在这个方法里面第一次调用 comparableClassFor(key) 时进行缓存
         * The kc argument caches comparableClassFor(key) upon first use
         * comparing keys.
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                // ph = p.hash, dir 指示从左子树查找还是右子树查找
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    // p.key 和 key 比较，只有当 p.key == key || k.equals(pk)时才是相等
                    // 找到了，返回
                    return p;
                else if (pl == null)
                    // p左儿子儿子为空，所以只能从右儿子开始找
                    p = pr;
                else if (pr == null)
                    // p右儿子为空，所以只能从左儿子开始找
                    p = pl;
                // 走到这里说明哈希值相等 且 p.key != k 且 k.equals(pk) == false 且 左右子树都不为空
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) &&
                         (dir = compareComparables(kc, k, pk)) != 0)
                    // comparable 比较结果不相等
                    p = (dir < 0) ? pl : pr;

                // 哈希值相等 且 ( kc == null 或者comparable比较结果相等)，这种情况下没有办法判断是从左子树
                // 去查找还是从右子树去查找，所以左右子树都要查找

                // 首先从右子树去查找，没有找到再从左子树去查找
                // 这里先从左子树去查找可能会好一点，因为比较结果相等的元素都插入在左子树，旋转后才会到右子树
                else if ((q = pr.find(h, k, kc)) != null)
                    // 找到了返回
                    return q;
                else
                    // 从右子树中没有找到，继续从左子树中查找
                    p = pl;
            } while (p != null);

            // 如果没有找到 p.key == key || k.equals(pk) 的元素，那么返回null
            return null;
        }

        /**
         * Calls find for root node.
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            // root.find()
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                    // 比较类名
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                // 如果类名比较结果相同,使用系统生成的哈希值进行判断
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * Forms tree of the nodes linked from this node.
         * @return root of tree
         */
        // 只能使用链表的第一个节点调用这个方法
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                // 取出TreeNode的下一个节点
                next = (TreeNode<K,V>)x.next;

                // 插入树之前先把 x.left 和 x.right设置为null
                // treeifyBin()方法中是重新new TreeNode()的，所以似乎没什么必要
                // --> 扩容的时候有用，扩容之前是红黑树，扩容后还是红黑树的情况下有用
                x.left = x.right = null;

                if (root == null) {
                    // 如果根节点不存在，那么 root = x，并设置颜色为黑色
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                else {
                    // 根节点已存在，把节点插入到相应的位置

                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    // 把链表转成红黑树不需要判断key是否已存在，因为key肯定都是不一样的才会插入进来
                    for (TreeNode<K,V> p = root;;) {
                        // dir: direction -> 指示存储在左子树还是右子树
                        // ph = p.hash
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        // 先使用哈希值判断，哈希值相等再使用Comparable判断(如果插入的键类型是class C implements Comparable<C> )
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            // key不是class C implements Comparable<C>类型，或者 comparable比较结果相等的话，再使用类名判断，
                            // 如果类名还是相同，再使用系统生成的哈希值进行判断。最后的结果还是可能是相等的，即执行完成之后dir可能还是0
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;
                        // 根节点开始，当 (p = (dir <= 0) ? p.left : p.right) == null的时候，xp就是插入元素x的父节点
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            // balanceInsertion()平衡红黑树，并返回新的根节点
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }

            // 确保根节点是在容器的第一个节点
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         */
        final Node<K,V> untreeify(HashMap<K,V> map) {
            // hd -> head; tl -> 上一个节点
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
                // 把TreeNode转成Node， next 赋值为null
                Node<K,V> p = map.replacementNode(q, null);
                if (tl == null)
                    // 第一个节点
                    hd = p;
                else
                    // 连接到上一个节点的后面
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * Tree version of putVal.
         */
        // ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value); TreeNode 调用的这个方法
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            // h -> 哈希值
            Class<?> kc = null;
            boolean searched = false;

            // 树状容器的根通常是它的第一个节点。但是，有时(目前仅在Iterator.remove之后)，
            // 根可能在其他地方，但是可以通过父链接(方法TreeNode.root())恢复。

            // parent != null 重新计算根节点，parent == null 说明this就是根节点
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                // dir: direction -> 插入左节点还是右节点
                // ph -> p的哈希值
                // pk -> p的键
                int dir, ph; K pk;

                // p 的哈希值大于插入key 的哈希值
                if ((ph = p.hash) > h)
                    // -1 -> 在左子树
                    dir = -1;
                else if (ph < h)
                    // 1 -> 在右子树
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    // p.key == key 或者 equals()方法返回true才是真正的相等，comparable只是比较一下存放的位置
                    // key 已存在, 返回已存在的节点，putVal()方法会判断是否更新原来的值
                    return p;

                // 先判断哈希值，哈希值相等且equals()方法返回false时才会使用 comparable判断
                else if ((kc == null &&
                        // comparableClassFor(k) 判断k 是否是 class C implements Comparable<C>
                          (kc = comparableClassFor(k)) == null) ||

                        // k -> 要插入的元素的key， pk -> p的 key
                        // 使用comparable 比较，若比较结果相等也相等(不能判断插入的元素是在左边还是在右边)
                         (dir = compareComparables(kc, k, pk)) == 0) {

                    // kc == null 或者 ((Comparable)k).compareTo(x)) == 0 才会进入这里，没办法判断是插入到左
                    // 子树还是插入到右子树，因此需要先从其子树中去先查找一下，判断key是否已经存在树中，存在则返回，
                    // 不进行插入，不存在再去插入

                    // searched 初始值为false
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;

                        // p这个元素在上面已经判断过不相等了，所以只要从左儿子开始找，左儿子没有找到再从右儿子去找
                        // -- 为什么从左儿子开始找，而不是右儿子？ 因为哈希值dir = 0的时候，元素是存到左子树，只有旋转了
                        // 元素才可能跑到右子树，所以先从左儿子去找，找到元素的可能性比较大。
                        if (((ch = p.left) != null &&
                                // kc -> class C implements Comparable<C>中的C的类对象
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                // 通过上面的查找确定key在树中不存在，才能进行插入
                TreeNode<K,V> xp = p;
                // 哈希值相等，equals方法返回false的时候 dir = 0，等于0插入到左子树
                // dir = 0的情况：哈希值相等，equals方法返回false，且comparable比较结果为0，且key在红黑树中不存在
                // p == null 的时候, xp就是要就是插入节点的父节点
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    // xpn -> xp.next的缩写
                    // 如果xp已经有一个左孩子xpl，现在插入xp的右孩子xpr，那么插入之前 xp.next = xpl, xpl.pre = xp，
                    // 插入时需要做链表的修改 xpr.next = xpl， xpl.prev = xpr; xp.next = xpr, xpr.prev = xp
                    // 所以红黑树的节点 也是是链表结构的，并且还是双链表(有pre 和 next)

                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);  // 对应上面例子的：xpr.next = xpl
                    // 把节点插入红黑树中
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;        // 对应上面例子的：xp.next = xpr
                    x.parent = x.prev = xp;  // 对应上面例子的： xpr.prev = xp
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x; // 对应上面例子的：xpl.prev = xpr
                    // balanceInsertion() 红黑树平衡
                    // moveRootToFront() 确保红黑树的根是容器的第一个节点
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * 移除的元素必须存在，不能为空。因为是使用移除的元素来调用这个方法
         * Removes the given node, that must be present before this call.
         * 这比典型的红黑删除代码更复杂，因为我们不能使用由“next”指针固定的叶子
         * 继承器来交换内部节点的内容，而“next”指针在遍历过程中是独立可访问的。
         * This is messier than typical red-black deletion code because we
         * cannot swap the contents of an interior node with a leaf
         * successor that is pinned by "next" pointers that are accessible
         * independently during traversal.
         * 所以我们交换树的连杆。如果当前树的节点似乎太少，则将该bin转换回普通bin。(根据树结构，测试触发2到6个节点)
         * So instead we swap the tree linkages. If the current tree appears to have too few nodes,
         * the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        // movable -> 是否把根节点移动到链表的第一个位置
        // 注意： this就是当前要删除的节点
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            int n;
            // n = tab.length
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;

            // root = first， 连续进行删除操作，第一个节点未必是根节点，
            // 如果first不是真正的根节点，下面会重新计算根节点
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            // succ: successor
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;


            // ------------  修改链表结构 -----------------
            if (pred == null)
                // 若删除的节点是第一个节点，修改 tab[index] 和 first 为succ，
                // 即第一个节点指向了next，原来的第一个节点就从链表中断开了
                tab[index] = first = succ;
            else
                // pred.next 指向 succ，这样就将当前节点从链表中移除了(当前节点就是要删除的节点)
                pred.next = succ;
            if (succ != null)
                // 修改succ 的上一个节点
                succ.prev = pred;

            if (first == null) // 当前索引位置已经没有元素了
                return;
            if (root.parent != null) // root 不是真正的根节点
                // 重新计算根节点，连续进行删除操作，第一个节点未必是根节点
                root = root.root();

            if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
                // 若删除的节点是第一个节点，那么现在first已经指向 succ了
                // 节点太少了，将红黑树转成链表结构，前面删除的元素已经从链表中移除了
                tab[index] = first.untreeify(map);  // too small
                return;
            }

            // ----------  从红黑树中删除元素 ----------------

            // 经过上面的判断，从红黑树中删除元素，红黑树的结构深度至少以下是这样的，否则就直接把红黑树转成链表了


            //                    root
            //                    /  \
            //                  rl    rr
            //                  /
            //                 rll

            // this 就是要删除的节点
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                // 删除的元素左右孩子都不为空： s = pr
                TreeNode<K,V> s = pr, sl;
                //        p
                //       / \
                //     pl  s(pr)
                //         /
                //        sl
                // 1、查找右子树中最小的元素，比如上图中的s1
                while ((sl = s.left) != null) // find successor
                    s = sl;

                // 现在 s 是删除元素右子树中最小的元素
                // 2、 s 和 p 互换颜色和位置，然后再去删除p

                //   2.1 互换颜色
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                // 右子树中最小的元素左节点一定为空，所以只要先把s的右节点保存下来就行了
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;

                //  2.2 把S 和 P互换位置

                // 判断s(右子树最小的元素) 是否是 p的右节点，这个判断很重要，否则就会出现 p.parent = p
                if (s == pr) { // p was s's direct parent
                    // s 的右子树已经赋值给 sr了，下面会把sr 添加到 p的右子树
                    p.parent = s;   // p.right，p.left 还没修改，下面会去修改，和else情况是公共的逻辑
                    s.right = p;    // s.left，s.parent 还没修改，下面会去修改，和else情况是公共的逻辑
                }

                // s不是p的右节点
                else {
                    TreeNode<K,V> sp = s.parent;
                    // p.parent = sp
                    if ((p.parent = sp) != null) {
                        if (s == sp.left) // s 在 sp的左子树
                            sp.left = p;  // sp的左子树变成 p
                        else
                            sp.right = p; // sp的右子树变成 p
                    }
                    // s.right = pr
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }

                // p 和 s互换位置，s是p右子树的最小元素，所以s的左子树一定为空，换位置后 p.left = null
                p.left = null;

                // p.right = sr
                if ((p.right = sr) != null)
                    sr.parent = p;

                // s.left = pl
                if ((s.left = pl) != null)
                    pl.parent = s;

                // s.parent = pp
                if ((s.parent = pp) == null)
                    // pp == null，说明p是根节点，换位置后s变成根节点
                    root = s;

                // 修改pp的子节点
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;

                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }

            // 左右子树一个为空、或者都为空的情况
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;

            // ------ replacement != p 的 p 节点删除
            if (replacement != p) {
                // replacement != p，只要用 replacement替换 p就行了，然后颜色变成p的颜色即可

                // ----- 使用replacement节点替换p节点，然后变色是在balanceDeletion() 方法里面 ----

                // replacement.parent = p.parent
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    // 如果 p是根节点，那么现在 replacement 变成了根节点
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;

                // p的左右节点和父亲都设置为null
                p.left = p.right = p.parent = null;
            }

            // p的左右节点至少有一个为空，如果p 的颜色是红色的，那么p的左右节点肯定都为空(因为不能连续两个红色的节点)
            // 所以如果p 的颜色是红色的，那么直接删除即可

            // 删除的节点是黑色的，才会调用 balanceDeletion()方法
            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            // ------- replacement == p 的p 节点删除

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize; 只有 resize()方法会调用这个方法
         * see above discussion about split bits and indices.
         *
         * @param map the map
         * @param tab the table for recording bin heads
         * @param index the index of the table being split
         * @param bit the bit of hash to split on
         */
        // ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            // this -> 链表的第一个节点
            TreeNode<K,V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;

            // lc -> loCount;    hc -> hiCount
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                // e.next 可能不为null，所以这里先赋值为null，因为链表顺序需要重新排序了，
                // 而且排在最后的节点的next = null
                e.next = null;
                // bit = oldCap -> oldTab.length
                if ((e.hash & bit) == 0) {
                    // 赋值 e.prev = loTail
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                // 如果 loHead所在链表的长度小于6，那么把红黑树节点转成链表节点
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    // 如果 hiHead == null，说明原来的红黑树并没有被分割，还是一棵完整的红黑树，只是存储位置变了
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                // 长度小于6，转成链表结构
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    // 如果 loHead == null，说明原来的红黑树并没有被分割，还是一棵完整的红黑树，只是存储位置变了
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)  // p.right = r.left
                    // 若 r.left != null，修改 r.left.parent = p;
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null) // r.parent = p.parent
                    // 如果p.parent == null，那么p就是原来的根节点，旋转后r成为新的根节点
                    // root = r， 且设置颜色为黑色
                    (root = r).red = false;
                else if (pp.left == p)
                    // 如果p是pp 的左儿子，修改pp的左儿子为r
                    pp.left = r;
                else
                    // 如果p是pp 的右儿子，修改pp的右儿子为r
                    pp.right = r;
                // 修改r.left = p 和 p.parent = r
                r.left = p;
                p.parent = r;
            }
            // 返回新的根节点(红黑色的根节点而不是子树的根节点)
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {  // l = p.left
                if ((lr = p.left = l.right) != null)  // p.left = r.right
                    // 如果 r.right != null，修改r.right.parent = p
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)  // l.parent = p.parent
                    // 如果p.parent == null，那么p就是原来的根节点，旋转后r成为新的根节点
                    // 并设置颜色为黑色
                    (root = l).red = false;
                // 修改pp的儿子
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            // 返回新的根节点(红黑色的根节点而不是子树的根节点)
            return root;
        }

        // 方法返回根节点
        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    // 如果x 是根节点，x 变成黑色的
                    x.red = false;
                    // 返回的情况只有两种：
                    // 1、x是根节点的时候返回
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    // 2、 xp是黑色的了，说明没有两个连续红色的节点了，返回
                    // 如果xp是黑色的 或者 xp.parent == null (说明xp就是根节点，xp是根节点，xp不可能是红色的)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    // xp 是红色的，且xp 是xpp 的左子树
                    if ((xppr = xpp.right) != null && xppr.red) {
                        // xp的兄弟节点是红色的，这种情况不能通过旋转达到平衡，得上溯，
                        // 把 xppl(xp) 和xppr变成黑色，然后xp变成红色的 (xp是红色的，xpp肯定是黑色的)
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        // xp的兄弟节点是黑色的，可以通过旋转达到平衡，但是需要判断单旋转还是双旋转
                        if (x == xp.right) {
                            // 之字形，需要左右旋转。先进行左旋处理

                            //      xpp(b)                                   xpp(b)
                            //      /   \         旋转之前先把x赋值为xp       /    \
                            //    xp(r) xppr(b)   ====>  rotateLeft(x)    x(r)   xppr(b)
                            //      \                                     /
                            //      x(r)                               xp(r)

                            // 旋转之前先把x赋值为xp，通过旋转结果可以明了地看出为什么要把x赋值为xp
                            root = rotateLeft(root, x = xp);  // 旋转后返回新的根节点
                            // 现在x是指向图2中 xp的位置
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }


                        //          xpp(b)                    xp(b)
                        //          /    \                    /   \
                        //       xp(r)   xppr(b)   ===>     x(r)  xpp(r)
                        //        /                                 \
                        //     x(r)                                 xppr(b)

                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                // 旋转会返回新的根节点(根节点可能会在旋转后变化，也可能不变)
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    // xp 是红色的，且xp 是xpp 的右子树

                    if (xppl != null && xppl.red) {
                        // 上溯
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            // 之字形旋转，先进行右转
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        // x -> replacement
        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    // 如果删除的是根节点，那么会设置 root = s ，所以这种情况下 x != root
                    // x == root的情况:  1、上溯到根节点，直接返回，此时这棵树的所有路径少了一个黑色节点
                    //                  2、 设置 x = root，退出循环
                    return root;
                else if ((xp = x.parent) == null) {
                    // todo： xp == null 那么 x不就是根节点了吗? 根本不会进入到这里面? 每次旋转后 root都会指向新的根节点
                    // 什么情况下 root 不是根节点？

                    // xp == null 说明x 就是根节点，设置x的颜色为黑色
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    // replacement != p 的情况，也就是删除元素已经从树中删除掉的情况下，x的颜色一定是红色的，也就是在这一步就结束了

                    // x 的颜色为红色的，设置x的颜色为黑色即可
                    // 1、x不可能是删除的元素本身，因为如果删除的元素是红色的，不会调用这个方法
                    // 2、x可能是删除元素的左节点，或者右节点，此时已经使用x替换了删除元素的位置，只要把颜色设置为黑色即可
                    // 3、上溯过程中，如果x的颜色是红色的，设置为黑色的即可
                    x.red = false;
                    return root;
                }

                else if ((xpl = xp.left) == x) {
                    // x 是xp的左子树

                    // 走到这里说明 x是删除的元素本身而且颜色是黑色，且x 没有子节点
                    // 这时候需要分以下几种情况：
                    // 1、x的兄弟节点s是否是红色的
                    // 2、x的兄弟节点s是黑色，s是否有红色的孩子
                    // 3、x的兄弟节点s是黑色的，且没有红色的孩子，父节点是否是红色的
                    // 4、如果以上情况都不是，s设置为红色，然后上溯，最坏情况下，到达根节点，把根节点的另一个子节点变成红色的，
                    //    这样，整棵树的所有路径少了一个黑色的节点。上溯过程中也始终不会修改根节点的颜色，所以也不用维护根节点的颜色。

                    // xpr -> x的兄弟节点
                    if ((xpr = xp.right) != null && xpr.red) {
                        // 1、 x的兄弟节点是红色，通过旋转解决

//                    P(b)                          S(b)                      S(b)
//                   /   \         第一步           /   \       第二步        /   \
//                 D(b)  S(r)      ====>         P(r)  SR(b)    ====>       P(b)  SR(b)
//                       /  \                    /  \                      /  \
//                    SL(b) SR(b)              D(b) SL(b)                 D(r) SL(r)

                        // 这里只完成了上面的第一步
                        xpr.red = false;   // 兄弟节点颜色变成黑色的
                        xp.red = true;     // xp的颜色变成红色的
                        root = rotateLeft(root, xp);
                        // 旋转后位置变了，重新赋值： xp = x.parent； xpr = xp.right
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null) // todo： 什么情景？ x的颜色是黑色的，所以xpr不可能为空的
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {

                            // 2、x的兄弟节点xpr是黑色的，且xpr没有红色的孩子，把兄弟节点设置为红色，把 x 指向 xp 进行上溯
                            // 上面的 情景1 的第二步在这里得到解决
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            // 3、S 的左节点是红色的，通过旋转和颜色变换把场景变成场景4

//                    P(b)                         P(b)
//                   /   \                         /   \
//                 D(b)  S(b)      ====>         D(b)  SL(b)
//                       /                               \
//                    SL(r)                              S(R)

                            if (sr == null || !sr.red) { // 如果左右节点都是红色的，那么只需要进行单旋转就行了
                                if (sl != null)
                                    // sl 的颜色设置为黑色的
                                    sl.red = false;

                                xpr.red = true; // 把xpr颜色变成红的
                                root = rotateRight(root, xpr);

                                // xpr指向上图的SL位置
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }

                            // 4. S的右节点颜色是红色的
                            //   原理：旋转变色后右子树黑色节点个数不变，左子树黑色节点个数多了一个
//                    P(b)                         S(b)
//                   /   \                         /   \
//                 D(b)  S(b)      ====>         P(b)  SR(b)
//                         \                     /
//                         SR(r)                D(r)

//                    P(r)                         S(r)
//                   /   \                         /   \
//                 D(b)  S(b)      ====>         P(b)  SR(b)
//                         \                     /
//                         SR(r)                D(r)

                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red; // xp节点也可能是红的，所以设置成xp的颜色
                                if ((sr = xpr.right) != null)
                                    sr.red = false; // 设置sr颜色为黑色的
                            }
                            if (xp != null) {
                                xp.red = false; // 设置xp颜色为黑色的，对应上图的P节点
                                root = rotateLeft(root, xp);
                            }
                            // 通过x = root 退出循环
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    // 删除的元素在父节点的右边

                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        // 检查链表结构和红黑树结构是否正常
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;

            // 检查 tb(tp) 和 t的双向连接是否正确
            if (tb != null && tb.next != t)
                return false;

            // 检查 tn 和 t的双向连接是否正确
            if (tn != null && tn.prev != t)
                return false;

            // 检查 tp 和 t 的节点指向是否正确
            if (tp != null && t != tp.left && t != tp.right)
                return false;

            // 检查 tl 和 t的节点指向是否正确，并比较哈希值，tl的哈希值不能大于t的哈希值
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;

            // 检查 tr 和 t的节点指向是否正确，并比较哈希值，tr的哈希值不能小于t的哈希值
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;

            // 检查不能有两个连续的红色节点
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;

            // 递归检查左子树 (使用的是中序遍历)
            if (tl != null && !checkInvariants(tl))
                return false;

            // 递归检查右子树
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}
