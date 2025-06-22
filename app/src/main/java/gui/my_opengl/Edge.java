package gui.my_opengl;
public class Edge {
    public final int v1;
    public final int v2;

    public Edge(int v1, int v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Edge)) return false;
        Edge other = (Edge) obj;
        return this.v1 == other.v1 && this.v2 == other.v2;
    }

    @Override
    public int hashCode() {
        return 31 * v1 + v2;
    }
}

