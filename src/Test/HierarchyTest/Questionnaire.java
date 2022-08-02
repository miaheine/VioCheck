package Test.HierarchyTest;

public class Questionnaire {
    private String[] questions;
    private String[] answers;

    public Questionnaire(int size) {
        questions = new String[size];
        answers = new String[size];
    }

    public void {questionnaire} addQuestion(String question) {
        questions[questions.length] = question;
    }

    public void {questionnaire} addAnswer(String answer) {
        answers[answers.length] = answer;
    }

    public String[] {questionnaire} getAnswers() {
        useAnswers(answers);
        return answers;
    }

    public void {study} useAnswers(String[] answers) {
        publishAnswers(answers);
    }

    public void {marketing} publishAnswers(String[] answers) {
        System.out.println(answers);
    }


}
