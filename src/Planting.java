// Name: Joel Faubert
// Student id: 2560106
//
// The Planting Synchronization Problem
//

import java.util.concurrent.Semaphore;


public class Planting {

//	public static Collection<Hole> holes = new ArrayList<>();

    public static void main(String args[]) {


        int i;
        // Create Student, TA, Professor threads
        TA ta = new TA();
        Professor prof = new Professor(ta);
        Student stdnt = new Student(ta);

        // Start the threads
        prof.start();
        ta.start();
        stdnt.start();

        // Wait for prof to call it quits
        try {
            prof.join();
        } catch (InterruptedException e) {
        }
        // Terminate the TA and Student Threads
        ta.interrupt();
        stdnt.interrupt();

        System.out.println("Professor: We dug a total of : " + ta.getHoleDug() + " holes.");
        System.out.println("Professor: We planted a total of : " + ta.getHolePlanted() + " seeds.");
        System.out.println("Professor: We filled a total of : " + ta.getHoleFilled() + " holes.");
    }
}


class Student extends Thread {
    TA ta;

    public Student(TA taThread) {
        ta = taThread;
    }

    public void run() {

        StringBuilder msg = new StringBuilder();

        while (true) {

            try {
                msg.setLength(0);
                msg.append("Student: I can still dig " );
                msg.append(ta.getMAX() - (ta.getHoleDug() - ta.getHoleFilled()));
                msg.append(" holes.");
                System.out.println(msg);
                ta.canDig.acquire();
                // Can dig a hole - lets get the shovel
                ta.shovel.acquire();
            } catch (Exception e) {
                System.out.println(e);
            }

            System.out.println("Student: Got the shovel");

            try {
                sleep((int) (100 * Math.random()));
            } catch (Exception e) {
                break;
            } // Time to fill hole
            ta.incrHoleDug();  // hole filled - increment the number

            System.out.println("Student: Hole " + ta.getHoleDug() + " Dug");

            System.out.println("Student: Letting go of the shovel");

            ta.shovel.release();
            ta.readyToPlant.release();

            if (isInterrupted()) {
                System.out.println("Student: I think I'll stop here.");
                break;
            }
        }
        System.out.println("Student is done");
    }
}

class TA extends Thread {
    // Some variables to count number of holes dug and filled - the TA keeps track of things
    private int holeFilledNum = 0;  // number of the hole filled
    private int holePlantedNum = 0;  // number of the hole planted
    private int holeDugNum = 0;     // number of hole dug
    private final int MAX = 5;   // can only get 5 holes ahead

    // add semaphores - the professor lets the TA manage things.

    public Semaphore shovel = new Semaphore(1);
    public Semaphore canDig = new Semaphore(5);
    public Semaphore readyToPlant = new Semaphore(0);
    public Semaphore readyToFill = new Semaphore(0);


    public int getMAX() {
        return (MAX);
    }

    public void incrHoleDug() {
        holeDugNum++;
    }

    public int getHoleDug() {
        return (holeDugNum);
    }

    public void incrHolePlanted() {
        holePlantedNum++;
    }

    public int getHolePlanted() {
        return (holePlantedNum);
    }

    public void incrHoleFilled() {
        holeFilledNum++;
    }

    public int getHoleFilled() {
        return (holeFilledNum);
    }

    public TA() {
        // Initialise things here
    }

    public void run() {
        while (true) {

            try {
                // There's a hole to be filled
                readyToFill.acquire();
                // get the shovel
                shovel.acquire();
                System.out.println("TA: Got the shovel");
                sleep((int) (100 * Math.random()));
            } catch (Exception e) {
                System.out.println(e);
                break;
            } // Time to fill hole
            holeFilledNum++;  // hole filled - increment the number
            System.out.println("TA: The hole " + holeFilledNum + " has been filled");
            canDig.release();
            System.out.println("TA: Letting go of the shovel");
            shovel.release();
            if (isInterrupted()) {
                System.out.println("TA: I think I'll stop here.");
                break;
            }
        }
        System.out.println("TA is done");
    }
}

class Professor extends Thread {
    TA ta;

    public Professor(TA taThread) {
        ta = taThread;
    }

    public void run() {
        while (ta.getHolePlanted() <= 20) {

            try {
                ta.readyToPlant.acquire();
                sleep((int) (50 * Math.random()));
            } catch (InterruptedException e) {
                System.out.println("Professor: I guess I'll stop here");
                break;
            }
            catch (Exception e) {
                System.out.println(e);
                 }
            // Time to plant

            ta.incrHolePlanted();  // the seed is planted - increment the number
            System.out.println("Professor: All be advised that I have completed planting hole " +
                    ta.getHolePlanted());
            ta.readyToFill.release();
        }
        System.out.println("Professor: We have worked enough for today");
    }
}
