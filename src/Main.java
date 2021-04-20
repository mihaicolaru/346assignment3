public class Main {
    public static void main(String[] args) {
        //create mailbox
        mailbox mailbox = new mailbox();

        //create memory manager and thread, start thread
        Manager mmu = new Manager();
        Thread manager = new Thread(mmu);
        manager.start();

        //create scheduler and thread, start thread
        Scheduler scheduler = new Scheduler();
        Thread sched = new Thread(scheduler);
        sched.start();

        //create clock thread, start thread
        Thread clock = new Thread(Clock.INSTANCE);
        clock.start();

        try{
            //join all threads, set done values to true for mmu and clock
            sched.join();
            mmu.setDone(true);
            manager.join();
            Clock.INSTANCE.setComplete(true);
            clock.join();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
