package com.rothsCode.liteGateway.core.plugin.process.quene;


public interface Flusher<E> {

	/**
	 * <B>方法名称：</B>add<BR>
	 * <B>概要说明：</B>添加元素方法<BR>
	 *
	 * @param event
	 * @author roths
	 * @since 2021年12月7日 上午12:21:27
	 */
	void add(E event);

	/**
	 * <B>方法名称：</B>add<BR>
	 * <B>概要说明：</B>添加多个元素<BR>
	 *
	 * @param event
	 * @author roths
	 * @since 2021年12月7日 上午12:21:53
	 */
	void add(@SuppressWarnings("unchecked") E... event);

	/**
	 * <B>方法名称：</B>tryAdd<BR>
	 * <B>概要说明：</B>尝试添加一个元素, 如果添加成功返回true 失败返回false<BR>
	 *
	 * @param event
	 * @return
	 * @author roths
	 * @since 2021年12月7日 上午12:22:27
	 */
	boolean tryAdd(E event);

	/**
	 * <B>方法名称：</B>tryAdd<BR>
	 * <B>概要说明：</B>尝试添加多个元素, 如果添加成功返回true 失败返回false<BR>
	 *
	 * @param event
	 * @return
	 * @author roths
	 * @since 2021年12月7日 上午12:23:04
	 */
	boolean tryAdd(@SuppressWarnings("unchecked") E... event);

	/**
	 * <B>方法名称：</B>isShutdown<BR>
	 * <B>概要说明：</B>isShutdown<BR>
	 *
	 * @return
	 * @author roths
	 * @since 2021年12月7日 上午12:23:43
	 */
	boolean isShutdown();

	/**
	 * <B>方法名称：</B>start<BR>
	 * <B>概要说明：</B>start<BR>
	 *
	 * @author roths
	 * @since 2021年12月7日 上午12:23:48
	 */
	void start();

	/**
	 * <B>方法名称：</B>shutdown<BR>
	 * <B>概要说明：</B>shutdown<BR>
	 *
	 * @author roths
	 * @since 2021年12月7日 上午12:23:53
	 */
	void shutdown();


}
