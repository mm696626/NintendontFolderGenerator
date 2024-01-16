package validation;

public class ExtensionValidator {

    public boolean isExtensionValid(String fileName) {
        return fileName.endsWith("iso") || fileName.endsWith("ciso");
    }
    public boolean isISOFile(String fileName) {
        return fileName.endsWith("iso");
    }
}
