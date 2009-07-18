/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2005-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$

package scala.actors

import scala.collection.mutable.HashMap
import java.util.concurrent.{ThreadPoolExecutor, RejectedExecutionException}

/**
 * The <code>ThreadPoolScheduler</code> class uses an
 * <code>ExecutorService</code> to execute <code>Actor</code>s. It
 * does not start an additional thread.
 * 
 * A <code>ThreadPoolScheduler</code> attempts to shut down
 * the underlying <code>ExecutorService</code> only if
 * <code>terminate</code> is set to true.
 * 
 * Otherwise, the <code>ExecutorService</code> must be shut down either
 * directly or by shutting down the
 * <code>ThreadPoolScheduler</code> instance.
 * 
 * @author Philipp Haller
 */
class ThreadPoolScheduler(protected var executor: ThreadPoolExecutor,
                          protected var terminate: Boolean) extends TerminationService(terminate) {

  /* This constructor (and the var above) is currently only used to work
   * around a bug in scaladoc, which cannot deal with early initializers
   * (to be used in subclasses such as DefaultExecutorScheduler) properly.
   */
  def this() {
    this(null, true)
  }

  /** Submits a <code>Runnable</code> for execution.
   *
   *  @param  task  the task to be executed
   */
  def execute(task: Runnable) {
    try {
      executor execute task
    } catch {
      case ree: RejectedExecutionException =>
        // run task on current thread
        task.run()
    }
  }

  def executeFromActor(task: Runnable) =
    execute(task)

  def onShutdown() {
    executor.shutdown()
  }

  /** The scheduler is active if the underlying <code>ExecutorService</code>
   *  has not been shut down.
   */ 
  def isActive =
    (executor ne null) && !executor.isShutdown()

  override def managedBlock(blocker: ManagedBlocker) {
    val coreSize = executor.getCorePoolSize()
    if ((executor.getActiveCount() >= coreSize - 1) && coreSize < ThreadPoolConfig.maxPoolSize) {
      executor.setCorePoolSize(coreSize + 1)
    }
    blocker.block()
  }
}