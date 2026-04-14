package gui.projects;

public class FileItem {
    private final String name;
    private final boolean isFolder;
    private final String path;

    public FileItem(String name, boolean isFolder, String path) {
        this.name = name;
        this.isFolder = isFolder;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getPath() {
        return path;
    }
}
