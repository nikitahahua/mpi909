package org.kpi;

import mpi.*;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 1) {
            if (rank == 0) {
                System.out.println("This program requires at least 1 process to run.");
            }
            MPI.Finalize();
            return;
        }

        final int ARRAY_SIZE = 1000;
        int[] fullArray = null;
        int chunkSize = ARRAY_SIZE / size;
        int[] localArray = new int[chunkSize];
        Random random = new Random();

        if (rank == 0) {
            fullArray = new int[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                fullArray[i] = random.nextInt(100);
            }
            System.out.println("Process 0 generated array of size " + ARRAY_SIZE);
        }

        if (rank == 0) {
            for (int dest = 1; dest < size; dest++) {
                int startIndex = dest * chunkSize;
                MPI.COMM_WORLD.Send(fullArray, startIndex, chunkSize, MPI.INT, dest, 0);
            }
            System.arraycopy(fullArray, 0, localArray, 0, chunkSize);
        } else {
            MPI.COMM_WORLD.Recv(localArray, 0, chunkSize, MPI.INT, 0, 0);
        }

        long localSum = 0;
        for (int value : localArray) {
            localSum += value;
        }
        System.out.println("Process " + rank + " local sum: " + localSum);

        long[] globalSum = new long[1];
        MPI.COMM_WORLD.Reduce(new long[]{localSum}, 0, globalSum, 0, 1, MPI.LONG, MPI.SUM, 0);

        if (rank == 0) {
            System.out.println("Global sum: " + globalSum[0]);
        }

        MPI.Finalize();
    }

}