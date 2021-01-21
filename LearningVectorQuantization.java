package coursework;

import static java.lang.Math.*;

public class LearningVectorQuantization {
    int numberOfEpochs = 10000000; //к-сть ітерацій, яку плануємо пройти
    int indexOfEpoch = 0; //лічильний к-сті ітерацій

    double learningSpeed = 0.1; //коеф швидкості навчання

    double[][] weightMatrix = new double[4][2]; //матриця ваг
    int indexOf1stWeightArray; //індекси векторів, які використовуються в якості початкових ваг (у матриці ваг)
    int indexOf2ndWeightArray; //(не використовуються для зміни ваг)

    int currentVectorIndex; //індекс поточного вектора (з яким безпосередньо працюємо)

    double[] inputs = new double[4]; //входи
    double[] exits = new double[2]; //<виходи>

    double claster1 = 1.0;
    double claster2 = 0.0;

    double distanseTo1st;
    double distanseTo2nd;

    public void learn(double[][] learningVector) { //метод навчання нейронної мережі
        long startTime = System.currentTimeMillis(); //зміна для підрахунку часу навчання нейромережі

        setWeightMatrix(learningVector); //задаємо вагову матрицю
        currentVectorIndex = setStartVectorIndex(learningVector); //зад початк індекс вектора, з я ким будемо працювати

        do {
            indexOfEpoch++;

            /*обчислюємо відстані до кластерів*/
            distanseTo1st = pow(weightMatrix[0][0] - learningVector[currentVectorIndex][0] ,2) +
                            pow(weightMatrix[1][0] - learningVector[currentVectorIndex][1] ,2) +
                            pow(weightMatrix[2][0] - learningVector[currentVectorIndex][2] ,2) +
                            pow(weightMatrix[3][0] - learningVector[currentVectorIndex][3] ,2);

            distanseTo2nd = pow(weightMatrix[0][1] - learningVector[currentVectorIndex][0] ,2) +
                            pow(weightMatrix[1][1] - learningVector[currentVectorIndex][1] ,2) +
                            pow(weightMatrix[2][1] - learningVector[currentVectorIndex][2] ,2) +
                            pow(weightMatrix[3][1] - learningVector[currentVectorIndex][3] ,2);


            /*визначення переможця та зміна векторів ваг*/
            if (distanseTo1st < distanseTo2nd) {
                //якщо <переможець> - 1ший кластер (змінюємо ваги в 1ій колонці матриці ваг)

                if(learningVector[currentVectorIndex][4] == claster1/*1.0*/) {
                    //якщо переможець = кластеру поточного навчального вектора
                    weightMatrix[0][0] = weightMatrix[0][0] + learningSpeed * (learningVector[currentVectorIndex][0] - weightMatrix[0][0]);
                    weightMatrix[1][0] = weightMatrix[1][0] + learningSpeed * (learningVector[currentVectorIndex][1] - weightMatrix[1][0]);
                    weightMatrix[2][0] = weightMatrix[2][0] + learningSpeed * (learningVector[currentVectorIndex][2] - weightMatrix[2][0]);
                    weightMatrix[3][0] = weightMatrix[3][0] + learningSpeed * (learningVector[currentVectorIndex][3] - weightMatrix[3][0]);
                } else {
                    //якщо переможець != кластеру поточного навчального вектора
                    weightMatrix[0][0] = weightMatrix[0][0] - learningSpeed * (learningVector[currentVectorIndex][0] - weightMatrix[0][0]);
                    weightMatrix[1][0] = weightMatrix[1][0] - learningSpeed * (learningVector[currentVectorIndex][1] - weightMatrix[1][0]);
                    weightMatrix[2][0] = weightMatrix[2][0] - learningSpeed * (learningVector[currentVectorIndex][2] - weightMatrix[2][0]);
                    weightMatrix[3][0] = weightMatrix[3][0] - learningSpeed * (learningVector[currentVectorIndex][3] - weightMatrix[3][0]);
                }

            } else {
                //якщо <переможець> - 2ий кластер

                if(learningVector[currentVectorIndex][4] == claster2/*0.0*/) {
                    //якщо переможець = кластеру поточного навчального вектора
                    weightMatrix[0][1] = weightMatrix[0][1] + learningSpeed * (learningVector[currentVectorIndex][0] - weightMatrix[0][1]);
                    weightMatrix[1][1] = weightMatrix[1][1] + learningSpeed * (learningVector[currentVectorIndex][1] - weightMatrix[1][1]);
                    weightMatrix[2][1] = weightMatrix[2][1] + learningSpeed * (learningVector[currentVectorIndex][2] - weightMatrix[2][1]);
                    weightMatrix[3][1] = weightMatrix[3][1] + learningSpeed * (learningVector[currentVectorIndex][3] - weightMatrix[3][1]);
                } else {
                    //якщо переможець != кластеру поточного навчального вектора
                    weightMatrix[0][1] = weightMatrix[0][1] - learningSpeed * (learningVector[currentVectorIndex][0] - weightMatrix[0][1]);
                    weightMatrix[1][1] = weightMatrix[1][1] - learningSpeed * (learningVector[currentVectorIndex][1] - weightMatrix[1][1]);
                    weightMatrix[2][1] = weightMatrix[2][1] - learningSpeed * (learningVector[currentVectorIndex][2] - weightMatrix[2][1]);
                    weightMatrix[3][1] = weightMatrix[3][1] - learningSpeed * (learningVector[currentVectorIndex][3] - weightMatrix[3][1]);
                }
            }

            updateLearnSpeed();
            changeCurrentVector(learningVector);// подаємо наступний навча

        } while (!maxNumberOfEpochsIsReached()); //умова завершення навчання

        long endTime = System.currentTimeMillis();//фіксація часу закінчення навчання нейромережі
        long timeOfWork = endTime - startTime;
        float timeOfWorkInSeconds = timeOfWork / 1000F; //вираж в секундах

        System.out.println("\n<Квантування навчального вектора (LVQ)>");
        System.out.println("Навчання завершене за " + timeOfWorkInSeconds + " сек.");
        System.out.println("кількість ітерацій: " + numberOfEpochs);
    }

    public void updateLearnSpeed() {
        learningSpeed = 0.1 * (1 - (indexOfEpoch / numberOfEpochs ));
    }

    //змінюємо вектор
    public void changeCurrentVector(double[][] learningVector) {

        if (currentVectorIndex == learningVector.length - 1) { //якщо був останній
            currentVectorIndex = 0;
            do {
                if (indexOf1stWeightArray == currentVectorIndex || indexOf2ndWeightArray == currentVectorIndex) {
                    currentVectorIndex++;
                }

            } while(!(currentVectorIndex != indexOf1stWeightArray && currentVectorIndex != indexOf2ndWeightArray));

        } else { //якщо був НЕ останній
            currentVectorIndex ++;
            do {
                //якщо <cтав> останній, але відноситься до матриці ваг
                if (currentVectorIndex == learningVector.length - 1 && (indexOf1stWeightArray == currentVectorIndex ||
                        indexOf2ndWeightArray == currentVectorIndex)) { //якщо <cтав> останній
                    currentVectorIndex = 0; //повертаємося на початок
                }
                if (indexOf1stWeightArray == currentVectorIndex || indexOf2ndWeightArray == currentVectorIndex) {
                    currentVectorIndex++;
                }
            } while (!(currentVectorIndex != indexOf1stWeightArray && currentVectorIndex != indexOf2ndWeightArray));
        }
    }

    public int setStartVectorIndex(double[][] learningVector) { //задаємо індекс початкового вектора (який бере безпосередню участь у навчанні)
        int indexToReturn = 0;

        for(int index = 0; index <learningVector.length; index++) {
            if (index != indexOf1stWeightArray && index != indexOf2ndWeightArray) {
                indexToReturn = index;
                break;
            }
        }

        return indexToReturn;
    }

    public void setWeightMatrix(double[][] learningVector) { //задаємо вагову матрицю
        boolean learningVector1IsReady = false;
        boolean learningVector2IsReady = false;

        for (int raw = 0; raw < learningVector.length; raw++) {
            if (learningVector[raw][4] == 0 && !learningVector1IsReady) {
                indexOf1stWeightArray = raw;

                weightMatrix[0][0] = learningVector[raw][0];
                weightMatrix[1][0] = learningVector[raw][1];
                weightMatrix[2][0] = learningVector[raw][2];
                weightMatrix[3][0] = learningVector[raw][3];
                learningVector1IsReady = true;
            }
            if (learningVector[raw][4] == 1 && !learningVector2IsReady) {
                indexOf2ndWeightArray = raw;

                weightMatrix[0][1] = learningVector[raw][0];
                weightMatrix[1][1] = learningVector[raw][1];
                weightMatrix[2][1] = learningVector[raw][2];
                weightMatrix[3][1] = learningVector[raw][3];
                learningVector2IsReady = true;
            }

            if (learningVector1IsReady && learningVector2IsReady) {
                break;
            }
        }
    }

    public boolean maxNumberOfEpochsIsReached() { //метод для перевірки, чи пройдено всі ітерації
        if (indexOfEpoch == numberOfEpochs)
            return true;
        else
            return false;
    }

    public void test(double[][] testVectors) { //метод тестування нейронної мережі

        for (int index = 0; index < testVectors.length; index++) {
            distanseTo1st = pow(weightMatrix[0][0] - testVectors[index][0] ,2) +
                    pow(weightMatrix[1][0] - testVectors[index][1] ,2) +
                    pow(weightMatrix[2][0] - testVectors[index][2] ,2) +
                    pow(weightMatrix[3][0] - testVectors[index][3] ,2);

            distanseTo2nd = pow(weightMatrix[0][1] - testVectors[index][0] ,2) +
                    pow(weightMatrix[1][1] - testVectors[index][1] ,2) +
                    pow(weightMatrix[2][1] - testVectors[index][2] ,2) +
                    pow(weightMatrix[3][1] - testVectors[index][3] ,2);

            System.out.println("вектор: " + testVectors[index][0] + " " + testVectors[index][1] +
                               " " + testVectors[index][2] + " " + testVectors[index][3]);

            if (distanseTo1st < distanseTo2nd)
                System.out.println("колектор");
            else
                System.out.println("покришка");
        }

    }
}
