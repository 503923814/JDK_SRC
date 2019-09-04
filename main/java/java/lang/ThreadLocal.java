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

package java.lang;
import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 该类提供了线程局部 (thread-local) 变量。这些变量不同于它们的普通对应物，因为访问某个变量
 * （通过其 get 或 set 方法）的每个线程都有自己的局部变量，它独立于变量的初始化副本。
 * ThreadLocal 实例通常是类中的 private static 字段，它们希望将状态与某一个线程（例如，用户 ID 或事务 ID）相关联。
 *
 * This class provides thread-local variables.  These variables differ from
 * their normal counterparts in that each thread that accesses one (via its
 * {@code get} or {@code set} method) has its own, independently initialized
 * copy of the variable.  {@code ThreadLocal} instances are typically private
 * static fields in classes that wish to associate state with a thread (e.g.,
 * a user ID or Transaction ID).
 *
 * <p>For example, the class below generates unique identifiers local to each
 * thread.
 * A thread's id is assigned the first time it invokes {@code ThreadId.get()}
 * and remains unchanged on subsequent calls.
 * <pre>
 * import java.util.concurrent.atomic.AtomicInteger;
 *
 * public class ThreadId {
 *     // Atomic integer containing the next thread ID to be assigned
 *     private static final AtomicInteger nextId = new AtomicInteger(0);
 *
 *     // Thread local variable containing each thread's ID
 *     private static final ThreadLocal&lt;Integer&gt; threadId =
 *         new ThreadLocal&lt;Integer&gt;() {
 *             &#64;Override protected Integer initialValue() {
 *                 return nextId.getAndIncrement();
 *         }
 *     };
 *
 *     // Returns the current thread's unique ID, assigning it if necessary
 *     public static int get() {
 *         return threadId.get();
 *     }
 * }
 * </pre>
 * 只要线程是活动的，且{@code ThreadLocal}实例是可访问的，每个线程都持有对其线程本地变量副本的隐式引用
 * <p>Each thread holds an implicit reference to its copy of a thread-local
 * variable as long as the thread is alive and the {@code ThreadLocal}
 * instance is accessible;
 * 在线程消失后，它的所有线程本地实例副本都要进行垃圾回收(除非存在对这些副本的其他引用)。
 * after a thread goes away, all of its copies of
 * thread-local instances are subject to garbage collection (unless other
 * references to these copies exist).
 *
 * @author  Josh Bloch and Doug Lea
 * @since   1.2
 */

/**
 * 总结：
 *  一、插入元素：
 *      1、如果  ThreadLocalMap 已存在，那么调用set(ThreadLocal<?> key, Object value)方法设置元素:
 *        根据key的hash值计算目标位置，然后从目标位置开始扫描：
 *          1.1 找到目标元素，那么使用新的值替换原来的值，然后返回；
 *          1.2 找到第一个以过期的元素，那么执行replaceStaleEntry() 方法：
 *                  1.2.1 查找 ( staleSlot之前的空槽, staleSlot之后的空槽) 最左边的已过期的元素，假设为 slotToExpunge;
 *               1.2.2 扫描 (staleSlot之后, 空槽之前)的元素，如果存在目标元素，使用新的值替换原来的值，
 *                        并把目标元素移动到staleSlot索引位置;
 *                 1.2.3 从slotToExpunge开始，删除一定范围内已过期的元素 (如果 slotToExpunge = staleSlot, 即没有找到目标元素，
 *                        且 ( staleSlot之前的空槽, staleSlot) 和 (staleSlot, staleSlot之后的空槽) 之间没有过期元素，那么不
 *                         用进行删除).
 *         1.3 到达空槽 (没有找到目标元素，且查找过程中没有过期的元素)，把元素插入空槽;
 *         1.4 从插入位置的下一个位置开始扫描 log2(size)个元素，每当 找到过期的元素，删除过期元素，并重置 n = len，
 *               然后继续扫描 log2(n)个元素 (此时n = len);
 *         1.5 如果没有发现过期的元素(即没有删除元素)，那么此时需要判断，size >= threshold ?
 *             1.5.1 如果 size 没有达到阈值，那么插入完成；
 *             1.5.2 如果 size 达到阈值，那么调用rehash()方法;
 *                   (1) 扫描数组所有的元素，删除过期的元素；
 *                   (2) 判断 (size >= threshold - threshold / 4), 如果为 true，那么数组扩大一倍，对所有的元素进行重新哈希，
 *                        并修改阈值。
 *
 *       2、如果  ThreadLocalMap 不存在，创建ThreadLocalMap，放入第一元素，并赋值给线程的 threadLocals
 *
 *   二、删除元素：
 *      1、如果ThreadLocalMap 不存在，直接返回；
 *      2、如果ThreadLocalMap 存在，那么调用remove(ThreadLocal<?> key)删除元素：
 *          2.1 根据 key 的hash值计算目标位置，从目标位置开始查找直到空槽位置，如果找到目标元素，则进行删除，然后返回。
 *
 *   三、查找元素：
 *      1、如果ThreadLocalMap 不存在，或者没有找到目标元素，那么调用 setInitialValue()方法；
 *      2、如果 ThreadLocalMap存在，根据key 的hash值计算出目标位置，获取目标位置的元素，如果该元素为目标元素，则返回，
 *         否则调用getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) 方法进行查找；
 *         2.1 如果目标位置上的元素 e = null，即目标位置上没有元素，说明 key对应的元素不存在，直接返回null
 *         2.2 如果目标位置上有元素 e != null，那么一直往下查找，直到空槽位置，如果发现有过期元素，那么删除过期元素，
 *              找到目标元素直接返回目标元素，方法结束，否则返回 null.
 *
 *
 *  四、cleanSomeSlots(int i, int n) 方法只有在设置元素的时候会进行调用(set()方法)。
 *
 */

public class ThreadLocal<T> {

    /**
     * threadlocal依赖于附加到每个线程的每个线程的线性探测哈希映射
     * ThreadLocals rely on per-thread linear-probe hash maps attached
     * to each thread (Thread.threadLocals and
     * inheritableThreadLocals).
     * ThreadLocal对象充当键，通过threadLocalHashCode进行搜索。
     * The ThreadLocal objects act as keys,
     * searched via threadLocalHashCode.
     * 这是一个自定义哈希代码(仅在threadlocalmap中有用)，它消除了常见情况下的冲突，
     * 即连续构造的threadlocal由相同的线程使用，而在不太常见的情况下保持良好的行为。
     * This is a custom hash code
     * (useful only within ThreadLocalMaps) that eliminates collisions
     * in the common case where consecutively constructed ThreadLocals
     * are used by the same threads, while remaining well-behaved in
     * less common cases.
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * The next hash code to be given out. Updated atomically. Starts at
     * zero.
     */
    // 注意： 这个是静态变量，所有的实例对象都共用
    private static AtomicInteger nextHashCode =
        new AtomicInteger();

    /**
     * 连续生成的哈希码之间的差异，将隐式顺序线程本地id转换为接近最优扩散的乘法哈希值，用于大小为2的幂的表
     * The difference between successively generated hash codes - turns
     * implicit sequential thread-local IDs into near-optimally spread
     * multiplicative hash values for power-of-two-sized tables.
     */
    // 对应二进制： 110 0001 1100 1000 1000 0110 0100 0111
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Returns the next hash code.
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * Returns the current thread's "initial value" for this
     * thread-local variable.
     * 这个方法在第一次调用 get()时被调用，除非先调用 set()方法 (set()方法先调用了就不会再调用这个方法了)
     * This method will be invoked the first
     * time a thread accesses the variable with the {@link #get}
     * method, unless the thread previously invoked the {@link #set}
     * method, in which case the {@code initialValue} method will not
     * be invoked for the thread.
     * 通常，每个线程最多调用该方法一次，但是在随后调用{@link #remove}和{@link #get}时，可能会再次调用该方法。
     * Normally, this method is invoked at
     * most once per thread, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     *
     * <p>This implementation simply returns {@code null}; if the
     * programmer desires thread-local variables to have an initial
     * value other than {@code null}, {@code ThreadLocal} must be
     * subclassed, and this method overridden.  Typically, an
     * anonymous inner class will be used. 通常，将使用匿名内部类
     * 比如：ThreadLocal.withInitial(() -> "xxx");
     *
     * @return the initial value for this thread-local
     */
    protected T initialValue() {
        return null;
    }

    /**
     * Creates a thread local variable. The initial value of the variable is
     * determined by invoking the {@code get} method on the {@code Supplier}.
     *
     * @param <S> the type of the thread local's value
     * @param supplier the supplier to be used to determine the initial value
     * @return a new thread local variable
     * @throws NullPointerException if the specified supplier is null
     * @since 1.8
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        // SuppliedThreadLocal 继承ThreadLocal，重写 initialValue()方法
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * Creates a thread local variable.
     * @see #withInitial(java.util.function.Supplier)
     */
    public ThreadLocal() {
    }

    /**
     * 返回此线程局部变量的当前线程副本中的值。如果变量没有用于当前线程的值，
     * 则先将其初始化为调用 initialValue() 方法返回的值。
     * Returns the value in the current thread's copy of this
     * thread-local variable.  If the variable has no value for the
     * current thread, it is first initialized to the value returned
     * by an invocation of the {@link #initialValue} method.
     *
     * @return the current thread's value of this thread-local
     */
    public T get() {
        Thread t = Thread.currentThread();
        // 返回 t.threadLocals
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    /**
     * 不调用set()方法进行值设置是因为，set()方法可能被用户重写了
     * Variant of set() to establish initialValue. Used instead
     * of set() in case user has overridden the set() method.
     *
     * @return the initial value
     */
    private T setInitialValue() {
        // 获取初始值
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    /**
     * 将当前线程的此线程局部变量的副本设置为指定的值。 大多数子类将无需重写此方法，
     * 仅依靠initialValue()方法设置线程本地值的值。
     * Sets the current thread's copy of this thread-local variable
     * to the specified value.  Most subclasses will have no need to
     * override this method, relying solely on the {@link #initialValue}
     * method to set the values of thread-locals.
     *
     * @param value the value to be stored in the current thread's copy of
     *        this thread-local.
     */
    // 真正保存数据的 Map其实是放在Thread中的
    public void set(T value) {
        Thread t = Thread.currentThread();
        // t.threadLocals
        ThreadLocalMap map = getMap(t);
        if (map != null)
            // 插入数据
            map.set(this, value);
        else
            // 创建ThreadLocalMap 并放入线程中
            createMap(t, value);
    }

    /**
     * 调用 remove()方法后，如果在调用set()方法前调用get()方法，那么
     * get()方法会调用initialValue()重新进行初始化。这就导致initialValue()
     * 方法被多次调用
     * Removes the current thread's value for this thread-local
     * variable.  If this thread-local variable is subsequently
     * {@linkplain #get read} by the current thread, its value will be
     * reinitialized by invoking its {@link #initialValue} method,
     * unless its value is {@linkplain #set set} by the current thread
     * in the interim.  This may result in multiple invocations of the
     * {@code initialValue} method in the current thread.
     *
     * @since 1.5
     */
     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
     }

    /**
     * Get the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param  t the current thread
     * @return the map
     */
    ThreadLocalMap getMap(Thread t) {
        // 返回线程的 threadLocals
        return t.threadLocals;
    }

    /**
     * Create the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param t the current thread
     * @param firstValue value for the initial entry of the map
     */
    void createMap(Thread t, T firstValue) {
        // 创建ThreadLocalMap，放入第一元素，并赋值给线程的 threadLocals
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * Factory method to create map of inherited thread locals.
     * Designed to be called only from Thread constructor.
     *
     * @param  parentMap the map associated with parent thread
     * @return a map containing the parent's inheritable bindings
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * Method childValue is visibly defined in subclass
     * InheritableThreadLocal, but is internally defined here for the
     * sake of providing createInheritedMap factory method without
     * needing to subclass the map class in InheritableThreadLocal.
     * This technique is preferable to the alternative of embedding
     * instanceof tests in methods.
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * An extension of ThreadLocal that obtains its initial value from
     * the specified {@code Supplier}.
     */
    // 继承ThreadLocal， 重写 get() 方法
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    /**
     * ThreadLocalMap是一个定制的散列映射，只适合维护线程本地值。
     * 在ThreadLocal类之外不导出任何操作。类是包私有的，允许在类线程中声明字段。
     * 为了帮助处理非常大且长期存在的使用，哈希表条目使用WeakReferences作为键。
     * 但是，由于不使用引用队列，所以只有当表开始耗尽空间时，才保证删除陈旧的条目。
     * ThreadLocalMap is a customized hash map suitable only for
     * maintaining thread local values. No operations are exported
     * outside of the ThreadLocal class. The class is package private to
     * allow declaration of fields in class Thread.  To help deal with
     * very large and long-lived usages, the hash table entries use
     * WeakReferences for keys. However, since reference queues are not
     * used, stale entries are guaranteed to be removed only when
     * the table starts running out of space.
     */

    /**
     * 1、元素插入时，如果目标位置上已经有了其他元素，那么元素往后插入到下一个空槽位置；
     * 2、元素删除时，必须移动删除元素到下一个空槽位置之间不在目标位置上的元素，否则删除
     *    元素后的空槽将导致 目标位置在空槽之前但是存储在空槽之后的元素找不到。
     *
     *    比如A元素的目标索引在位置1，而索引 123之前都已经有其他元素了，索引4没有元素，
     *    所以A元素将储存到索引4的位置，现在删除了索引3的元素，索引3将变成空槽，查找A元素的时候
     *    会从目标位置1开始查找直到空槽位置，如果删除索引3的元素之后，A元素没有移动，那么查找A元素只会
     *    查到到索引2，索引3是空槽不会再继续查找，所以必须在删除元素之后，把A元素移动到索引3的位置
     *
     *    0     1     2     3     4     5
     */
    static class ThreadLocalMap {

        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         *     key = null时意味着这个key不再被引用，因此它可以从table中删除
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         */
        //
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                // key 值保存在父类中
                super(k);
                value = v;
            }
        }

        /**
         * The initial capacity -- MUST be a power of two.
         */
        // 初始容量，必须是2的次方
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary.
         * table.length MUST always be a power of two.
         */
        // 必要的时候可以进行扩容，长度必须是2的次方
        private Entry[] table;

        /**
         * The number of entries in the table.
         */
        private int size = 0;

        /**
         * The next size value at which to resize.
         */
        // 构造方法初始化为10
        private int threshold; // Default to 0

        /**
         * 将调整大小阈值设置为最坏情况下保持2/3的负载因子。
         * Set the resize threshold to maintain at worst a 2/3 load factor.
         */
        private void setThreshold(int len) {
            // threshold = 16 * 2 / 3 = 10;
            threshold = len * 2 / 3;
        }

        /**
         * Increment i modulo len.
         */
        // 计算下一个索引位置
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * Decrement i modulo len.
         */
        // 计算上一个索引位置
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * Construct a new map initially containing (firstKey, firstValue).
         * 延迟构建，至少拥有一个元素的时候才创建它
         * ThreadLocalMaps are constructed lazily, so we only create
         * one when we have at least one entry to put in it.
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            // INITIAL_CAPACITY = 16
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            // 设置size = 1
            size = 1;
            // threshold = 16 * 2 / 3 = 10;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * Construct a new map including all Inheritable ThreadLocals
         * from given parent map. Called only by createInheritedMap.
         *
         * @param parentMap the map associated with parent thread.
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * 获取与key关联的条目。这个方法本身只处理快速路径:直接命中现有键。
         * Get the entry associated with key.  This method
         * itself handles only the fast path: a direct hit of existing
         * 否则，它会在错过后转发给getEntryAfterMiss。这是为了最大限度地提高直接命中的性能，
         * 部分原因是通过使该方法易于线性化。
         * key. It otherwise relays to getEntryAfterMiss.  This is
         * designed to maximize performance for direct hits, in part
         * by making this method readily inlinable.
         *
         * @param  key the thread local object
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntry(ThreadLocal<?> key) {
            // 计算key 的目标位置
            int i = key.threadLocalHashCode & (table.length - 1);
            // 获取目标位置上的元素
            Entry e = table[i];
            if (e != null && e.get() == key)
                // 找到目标元素，直接返回
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * getEntry方法的版本，用于在键的直接哈希槽中找不到键时使用。
         * Version of getEntry method for use when key is not found in
         * its direct hash slot.
         *
         * @param  key the thread local object
         *            根据key的哈希值计算出的目标索引位置
         * @param  i the table index for key's hash code
         * @param  e the entry at table[i]
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            // 如果传入的e = null，即目标位置上没有元素，说明 key对应的元素不存在，直接返回null
            // 如果传入的 e != null，那么一直往下查找，直到空槽位置，如果期间有过期元素，那么删除过期元素
            while (e != null) {
                ThreadLocal<?> k = e.get();

                if (k == key)
                    // 找到目标元素，返回
                    return e;

                if (k == null)
                    // 删除过期元素
                    expungeStaleEntry(i);
                else
                    // 检查下一个元素
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * Set the value associated with key.
         *
         * @param key the thread local object
         * @param value the value to be set
         */
        private void set(ThreadLocal<?> key, Object value) {

            // 我们不像使用get()那样使用快速路径，因为使用set()创建新条目至少
            // 和替换现有条目一样常见，在这种情况下，快速路径往往会失败。
            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            // 计算key 所在的索引位置
            int i = key.threadLocalHashCode & (len-1);

            // 如果 i 对应的索引位置已经有存储的元素了
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                // e.get()调用的是父类Reference 的方法，获取对应的 ThreadLocal
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    // key已存在，替换成新的值
                    e.value = value;
                    return;
                }

                // 元素已过期
                if (k == null) {
                    // 替换过期元素的key和value
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            // 插入元素
            tab[i] = new Entry(key, value);
            int sz = ++size;

            // cleanSomeSlots()判断一定范围内的元素是否已经过期，过期则进行删除，有删除元素会返回true，没有删除元素返回false。
            // 当有移除元素时，容量肯定不会达到阈值，因此不需要再判断容量是否达到阈值
            if (!cleanSomeSlots(i, sz) && sz >= threshold) {
                // 容量达到阈值将进行扩容
                rehash();
            }
        }

        /**
         * Remove the entry for key.
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null; // 直到空槽位置
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    // this.referent = null;
                    e.clear();
                    // 删除目标元素
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * 用指定键的项替换set操作期间遇到的陈旧项。值参数中传递的值存储在条目中，
         * 无论指定键的条目是否已经存在。
         * Replace a stale entry encountered during a set operation
         * with an entry for the specified key.  The value passed in
         * the value parameter is stored in the entry, whether or not
         * an entry already exists for the specified key.
         *
         * 作为一个副作用，这个方法删除了包含陈旧条目的“run”中的所有陈旧条目。(run 是两个空槽之间的条目序列)。
         * As a side effect, this method expunges all stale entries in the
         * "run" containing the stale entry.  (A run is a sequence of entries
         * between two null slots.)
         *
         * @param  key the key
         * @param  value the value to be associated with key
         *               搜索key时遇到的第一个陈旧条目的索引。
         * @param  staleSlot index of the first stale entry encountered while
         *         searching for key.
         */
        // 替换元素会扫描staleSlot 前后两个空槽之间的元素，并删除已过期的元素
        private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                       int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // 备份以检查当前run中以前的过期条目。
            // Back up to check for prior stale entry in current run.
            // 我们一次清理所有的runs，以避免由于垃圾收集器释放了大量的引用而导致的连续的增量重哈希
            // (例如，，每当收集器运行时)。
            // We clean out whole runs at a time to avoid continual
            // incremental rehashing due to garbage collector freeing
            // up refs in bunches (i.e., whenever the collector runs).

            // 查找 ( null元素之后, staleSlot之前] 最接近null元素 的过期元素的索引
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len);
                 (e = tab[i]) != null;      // 直到遇到null 元素位置
                 i = prevIndex(i, len)) {

                if (e.get() == null)
                    slotToExpunge = i;
            }

            // 找到run的键或后面的空槽，无论哪个先出现
            // Find either the key or trailing null slot of run, whichever
            // occurs first
            for (int i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;  // 直到空槽
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                // 如果找到key，则需要将其与陈旧的条目交换，以保持哈希表的顺序。然后，
                // 可以将新过期的插槽或上面遇到的任何其他过期插槽发送到expungeStaleEntry，
                // 以删除或重新散列运行中的所有其他条目。
                // If we find key, then we need to swap it
                // with the stale entry to maintain hash table order.
                // The newly stale slot, or any other stale slot
                // encountered above it, can then be sent to expungeStaleEntry
                // to remove or rehash all of the other entries in run.

                if (k == key) {     // 找到目标ThreadLocal
                    // 值替换成新的值
                    e.value = value;

                    // 把目标元素和过期的元素互换位置 ( 在删除元素之前staleSlot一定是距离目标位置最近的位置)
                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // 如果前一个陈旧条目存在，则从该条目开始删除
                    // Start expunge at preceding stale entry if it exists

                    // (slotToExpunge有可能在staleSlot之前，也可能在之后)
                    if (slotToExpunge == staleSlot)
                        // 如果 (null元素, staleSlot)之间没有过期的元素，则从 i开始删除 ( staleSlot位置已替换成目标元素)
                        slotToExpunge = i;

                    // 从slotToExpunge开始 删除过期元素一定范围内的所有过期的元素， expungeStaleEntry()会对
                    // slotToExpunge之后，空槽之前 的元素进行重新hash，并返回空槽位置，cleanSomeSlots()从返回的
                    // 空槽位置开始扫描，至少扫描 log2(len)个元素，删除扫描到的过期元素
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);

                    // 方法返回
                    return;
                }

                // If we didn't find stale entry on backward scan, the
                // first stale entry seen while scanning for key is the
                // first still present in the run.
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // ----------        for循环 结束 -----------------

            // 目标元素没有找到，
            // If key not found, put new entry in stale slot
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // If there are any other stale entries in run, expunge them
            if (slotToExpunge != staleSlot)
                // 如果staleSlot前后两个空槽之间存在过期元素，则从距离左边空槽最近的元素开始i，
                // 删除过期元素一定范围内的所有过期的元素
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         * 通过重新哈希位于staleSlot和下一个空槽之间的任何可能发生冲突的条目，删除陈旧的条目。
         * 这还将删除尾随null之前遇到的任何其他陈旧条目 (staleSlot 到 下一个空槽之间的元素)
         *
         * Expunge a stale entry by rehashing any possibly colliding entries
         * lying between staleSlot and the next null slot.  This also expunges
         * any other stale entries encountered before the trailing null.  See
         * Knuth, Section 6.4
         *
         * @param staleSlot index of slot known to have null key
         *         返回staleSlot 之后的下一个null元素的索引
         * @return the index of the next null slot after staleSlot
         * (all between staleSlot and this slot will have been checked
         * for expunging).
         */

        // 删除staleSlot 索引位置上的元素，并移动staleSlot之后的 不在根据hash值计算出的位置上的元素，(直到遇到null元素)
        // 移动到根据hash值计算出的位置上，如果该位置上已经有元素了，则移动到距离最近的没有元素的位置上，
        // 在这个过程中不会再删除key已被垃圾回收器回收的元素，直到遇到null元素

        // 移动 (staleSlot, null元素) 之间不在目标位置上的元素很重要，因为元素储存在目标位置之后要求和目标位置之间
        // 不能有空槽，否则就会导致查不到这个元素

        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            // 删除staleSlot索引上的 entry -- 设置值为 null，且设置tab在该索引上的元素为 null
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            // size - 1
            size--;

            // 重新哈希staleSlot到下一个null元素之间的元素
            // Rehash until we encounter null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;  // 遇到空元素就不再判断了
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                // 遇到陈旧的元素，则进行删除
                if (k == null) {
                    // 删除陈旧 entry (ThreadLocal已被垃圾回收器回收)
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    // 元素尚未过期则 重新hash该元素
                    int h = k.threadLocalHashCode & (len - 1);
                    // 如果元素不在根据hash值计算出的位置上
                    if (h != i) {
                        // 移动元素，先设置该索引位置上的元素为null.
                        tab[i] = null;

                        // 与Knuth 6.4算法R不同，我们必须扫描到null，因为多个条目可能已经过期。
                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null)
                            // 从根据hash值计算出的位置开始往下查找，tab[i] = null, 所以最坏情况下也只会到达位置 i
                            // 查找过程中，没有再删除已经过期的元素
                            h = nextIndex(h, len);

                        // 把元素e 移动到根据hash值计算出的位置上，如果该位置上已经有元素了，
                        // 则移动到距离最近的没有元素的位置上
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * 启发式地扫描一些单元格，寻找陈旧的entries
         * Heuristically scan some cells looking for stale entries.
         * 在添加新元素或删除另一个陈旧元素时调用此函数
         * This is invoked when either a new element is added, or
         * another stale one has been expunged.
         * 它执行对数次扫描，以平衡不扫描(快速但保留垃圾)和与元素数量成比例的
         * 扫描数量之间的关系，这将找到所有垃圾，但会导致一些插入花费O(n)时间。
         * It performs a
         * logarithmic number of scans, as a balance between no
         * scanning (fast but retains garbage) and a number of scans
         * proportional to number of elements, that would find all
         * garbage but would cause some insertions to take O(n) time.
         *
         *  已知不是持有陈旧 entry的位置。从 i + 1处开始扫描
         * @param i a position known NOT to hold a stale entry. The
         * scan starts at the element after i.
         *
         *   扫描{@code log2(n)}单元格，除非找到陈旧的条目，在这种情况下，扫描{@code log2(table.length)-1}其他单元格。
         * @param n scan control: {@code log2(n)} cells are scanned,
         * unless a stale entry is found, in which case
         * {@code log2(table.length)-1} additional cells are scanned.
         * 当从插入调用时，这个参数是元素的数量，但是当从replaceStaleEntry调用时，它是表长度
         * When called from insertions, this parameter is the number
         * of elements, but when from replaceStaleEntry, it is the
         * 注意:所有这些都可以通过加权n来改变或多或少的侵略性，而不是直接使用log n。
         *          但是这个版本简单、快速，而且似乎工作得很好。
         * table length. (Note: all this could be changed to be either
         * more or less aggressive by weighting n instead of just
         * using straight log n. But this version is simple, fast, and
         * seems to work well.)
         *
         *          如果有移除陈旧的 entry，则返回true
         * @return true if any stale entries have been removed.
         */

        /**
         * 1、从索引i 的下一个索引位置开始，扫描log2(n)个元素，每当找到一个陈旧的元素，
         *    将重置 n = len，然后继续扫描 log2(n)个元素 (此时n = len);

         * 2、如果扫描到陈旧的元素，假设索引为staleSlot，将删除 staleSlot这个元素，并重新哈希staleSlot 到下一个空槽之间的元素，
         *    把元素移动到根据hash值计算出的位置上，如果该位置上已经有元素了，则移动到距离最近的没有元素的位置上，重新哈希的过程中
         *    会删除陈旧的元素。
         */

        // 执行对数次扫描，在不扫描(快速但保留垃圾)和全部扫描(不会保留垃圾，但费时)之间取一个折中的方法。
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                // 从i的下一个位置开始查找, 扫描 log n 个元素
                i = nextIndex(i, len);
                Entry e = tab[i];
                // 找到陈旧的元素 (ThreadLocal已被垃圾回收器回收)
                if (e != null && e.get() == null) {
                    // n = len，每当找到一个陈旧的元素，将重置 n = len，
                    // 继续查找 log2(table.length)个元素，并不是 log2(table.length)-1
                    n = len;
                    removed = true;

                    // 返回staleSlot 之后的下一个null元素的索引， null元素之前的所有判断都已经判断过是否过期了
                    // (并且进行了重新哈希)，过期会进行删除
                    i = expungeStaleEntry(i);
                }
                // (n = n / 2) != 0
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        /**
         * 重新打包和/或调整表的大小。首先扫描整个表，删除陈旧的条目。如果这还不足以缩小表的大小，则将表的大小加倍。
         * Re-pack and/or re-size the table. First scan the entire
         * table removing stale entries. If this doesn't sufficiently
         * shrink the size of the table, double the table size.
         */
        private void rehash() {
            // 扫描整个数组所有的元素，删除过期的元素
            expungeStaleEntries();

            // 使用较低的阈值加倍，以避免滞后
            // 若 threshold = 10， 则删除所有的过期元素后， size >= 8 就会进行扩容
            // Use lower threshold for doubling to avoid hysteresis
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * 容量扩大一倍
         * Double the capacity of the table.
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;

            // 长度扩大一倍
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];

                // 若元素不为null
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        // 若元素已经过期，设置 value = null
                        e.value = null; // Help the GC
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        // 查找距离h 最近的尚未存入元素的索引位置
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        // 元素数量 + 1
                        count++;
                    }
                }
            }

            // 修改阈值， threshold = len * 2 / 3
            setThreshold(newLen);
            // 把 count 赋值给 size
            size = count;
            // 把新的数组赋值给 table
            table = newTab;
        }

        /**
         * Expunge all stale entries in the table.
         */
        // 扫描所有的元素，删除过期的元素
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    // 删除过期元素，并重新哈希j到下一个空槽位置上的元素，若发现过期元素则进行删除
                    expungeStaleEntry(j);
            }
        }
    }
}
