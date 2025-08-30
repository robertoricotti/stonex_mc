package event_bus;

public class CanRow {
    int id;
    String text;

    CanRow(int id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public String toString() {
        return text; // così l’ArrayAdapter mostra il testo
    }
}
