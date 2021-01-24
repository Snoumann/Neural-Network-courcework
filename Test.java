package coursework;

public class Test {

    public static void main(String[] args) {
        LearningVector learningVectors = new LearningVector(); //створюємо навчальні вектори
        learningVectors.showLearningVector();                  //виводимо

        BoltzmannMachine bMachine = new BoltzmannMachine();    //створюємо машину Больцмана
        bMachine.initializeAllWages();                         //ініціалізуємо всі ваги
        bMachine.learn(learningVectors.learningVector);        //підкл навч вектори та починаємо навчання м. Больцмана
        System.out.println("перевірка на навчальному векторі");
        bMachine.test(learningVectors.learningVector);         //проганяємо навчальний вектор через м. Больцмана
        TestingVector testingVectors = new TestingVector();    //створюємо тестові вектори
        testingVectors.showTestingVector();
        bMachine.test(testingVectors.testingVector);           //проганяємо тестовий вектор через м. Больцмана

        LearningVectorQuantization LVQ = new LearningVectorQuantization();
        LVQ.learn(learningVectors.learningVector);
        System.out.println("\nперевірка на навчальному векторі");
        LVQ.test(learningVectors.learningVector);
        System.out.println("\nперевірка на тестовому векторі");
        LVQ.test(testingVectors.testingVector);


    }
}
