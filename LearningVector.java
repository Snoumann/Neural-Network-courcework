package coursework;

public class LearningVector {
    double[][] learningVector; //масив вхідних тестових сигналів

    LearningVector() {
        learningVector = new double[][] {{18.9,  10.0,  8.6,   22, 1}, //колектори
                                         {12.6,  15.1,  14.4,  10, 1},
                                         {14.1,  7.8,   12.3,  12, 1},
                                         {15.0,  9.5,   12.8,  8,  1},
                                         {15.6,  12.4,  9.0,   17, 1},
                                         {17.8,  16.7,  7.5,   5,  1},
                                         {10.7,  22.2,  11.9,  14, 1},
                                         {11.5,  17.4,  18.2,  7,  1},

                                         {10.9,  15.6,  17.9,  8,  0},  //покришки
                                         {12.6,  40.1,  8.5,   4,  0},
                                         {9.5,   27.8,  12.4,  5,  0},
                                         {8.8,   18.9,  25.0,  3,  0},
                                         {12.0,  33.5,  8.6,   3,  0},
                                         {9.0,   14.7,  19.7,  7,  0},
                                         {8.5,   15.0,  22.4,  4,  0}};
    }

    public void showLearningVector() {
        System.out.println("\t\t  НАВЧАЛЬНІ ВЕКТОРИ");

        for (int raw = 0; raw < learningVector.length; raw++) {

            if (raw + 1 <= 8)
                System.out.print("K-" + (raw+1) + "     ");
            else
                System.out.print("П-" + (raw-7) + "     ");

            for (int col = 0; col < learningVector[raw].length; col++) {
                System.out.print(String.format("%4.1f", learningVector[raw][col]) + " ");
            }
            System.out.println();
        }
    }
}
