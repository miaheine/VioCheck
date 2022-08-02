package Test.HierarchyTest;

public class Record {
    private String name;
    private int cpr;
    private String disease;
    
    public void {research} setRecord(int cpr, String name) {
        this.cpr = cpr;
        this.name = name;
    }

    public void {research} setDisease(String disease) {
        this.disease = disease;
    }

    public String {research} getRecord() {
        return cpr + name + disease;
    }

    public void {research} useRecord() {
        Study study = new Study();
        study.addParticipant(getRecord());
    }
}
