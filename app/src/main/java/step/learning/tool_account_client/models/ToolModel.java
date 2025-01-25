package step.learning.tool_account_client.models;

public class ToolModel {
    private String id;
    private String name;
    private String model;
    private String number;
    private String firstname;
    private String lastname;

    public ToolModel(String id, String name, String model, String number, String firstname, String lastname) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.number = number;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public ToolModel(String id, String name, String model, String number) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.number = number;
        this.firstname = ""; // Або null
        this.lastname = ""; // Або null
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getNumber() { return number; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
}
