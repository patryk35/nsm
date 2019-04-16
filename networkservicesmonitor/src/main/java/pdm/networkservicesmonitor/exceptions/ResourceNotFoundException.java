package pdm.networkservicesmonitor.exceptions;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException( String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        additionalEntries.put("resourceName", resourceName);
    }
}