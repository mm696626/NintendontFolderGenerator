package validation;

public class ExtensionValidator {

    public boolean isExtensionValid(String fileName) {
        return fileName.endsWith("iso") || fileName.endsWith("ciso");
    }
}
