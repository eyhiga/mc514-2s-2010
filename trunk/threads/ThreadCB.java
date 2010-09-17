package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads
*/
public class ThreadCB extends IflThreadCB 
{
    private static GenericList ReadyQueue;

    /**
       The thread constructor. Must call 

       	   super();

       as its first statement.

       @OSPProject Threads
    */
    public ThreadCB()
    {
        super();
        ReadyQueue = new GenericList();

    }

    /**
       This method will be called once at the beginning of the
       simulation. The student can set up static variables here.
       
       @OSPProject Threads
    */
    public static void init()
    {

    }

    /** 
        Sets up a new thread and adds it to the given task. 
        The method must set the ready status 
        and attempt to add thread to task. If the latter fails 
        because there are already too many threads in this task, 
        so does this method, otherwise, the thread is appended 
        to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
	    if(task.getThreadCount() < IflThreadCB.MaxThreadsPerTask) {
	    	
            ThreadCB thread = new ThreadCB();

			if(task.addThread(thread) == FAILURE) {
			    return null;
			}
					
			thread.setTask(task);	
			thread.setStatus(ThreadReady);
			ThreadCB.ReadyQueue.append(thread);
			ThreadCB.dispatch();
			return thread;

			} else {
				  ThreadCB.dispatch();
			      return null;
			}
    }

    /** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.
        
	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.
	
	@OSPProject Threads
    */
    public void do_kill()
    {
        // your code goes here

    }

    /** Suspends the thread that is currenly on the processor on the 
        specified event. 

        Note that the thread being suspended doesn't need to be
        running. It can also be waiting for completion of a pagefault
        and be suspended on the IORB that is bringing the page in.
	
	Thread's status must be changed to ThreadWaiting or higher,
        the processor set to idle, the thread must be in the right
        waiting queue, and dispatch() must be called to give CPU
        control to some other thread.

	@param event - event on which to suspend this thread.

        @OSPProject Threads
    */
    public void do_suspend(Event event)
    {
     
        /*int status = this.getStatus();*/
				GenericList WaitingQueue;
        
    		/* Verifica status da thread */
        if(getStatus() == ThreadRunning) {
        	setStatus(ThreadWaiting);
        } else {
        	setStatus(getStatus()+1);
        }
        
        /* Remove thread da lista de Ready e insere na lista de Waiting */
        ThreadCB.ReadyQueue.remove(this);
        WaitingQueue = event.getThreadList();
				WaitingQueue.append(this);
				dispatch();

    }

    /** Resumes the thread.
        
	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.
	
	@OSPProject Threads
    */
    public void do_resume()
    {
 
			if(getStatus() >= ThreadWaiting) {
				/*this.setStatus(ThreadWaiting-1);*/
				if(getStatus() == ThreadWaiting) {
					setStatus(ThreadReady);
					ThreadCB.ReadyQueue.append(this);
    		} else {
					setStatus(getStatus()-1);
				}
					dispatch();
			}

		}

    /** 
        Selects a thread from the run queue and dispatches it. 

        If there is just one theread ready to run, reschedule the thread 
        currently on the processor.

        In addition to setting the correct thread status it must
        update the PTBR.
	
	@return SUCCESS or FAILURE

        @OSPProject Threads
    */
    public static int do_dispatch()
    {
    	/*int stat;
    	
    	stat = this.getStatus();
        MyOut.print("osp.Threads.ThreadsCB", ">>>>"+ThreadRunning);    	
        MyOut.print("osp.Threads.ThreadsCB", "===="+stat);*/

				TaskCB    currentTask;
				ThreadCB  currentThread;
				PageTable currentPage;
				TaskCB 		newTask;
				ThreadCB 	newThread;
				PageTable newPage;

				/* Verifica se existe uma thread na ReadyQueue 
				 * e faz a troca de contexto */
				newThread = (ThreadCB)ThreadCB.ReadyQueue.removeHead();
				if(newThread != null) {

					/* Seta o status ThreadReady na thread atual */
					currentPage = MMU.getPTBR();
					if(currentPage != null) {
						currentTask = currentPage.getTask();
						currentThread = currentTask.getCurrentThread();
						currentThread.setStatus(ThreadReady);
						MMU.setPTBR(null);
						currentTask.setCurrentThread(null);
					}

					/* Coloca a nova thread para rodar, utilizando
				 	* algoritmo First In, First Served  */
					newThread.setStatus(ThreadRunning);
					newTask = newThread.getTask();
					newPage = newTask.getPageTable();
					MMU.setPTBR(newPage);
					newTask.setCurrentThread(newThread);

					return SUCCESS;

				} else {

					return FAILURE;

				}
    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures in
       their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Threads
    */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
