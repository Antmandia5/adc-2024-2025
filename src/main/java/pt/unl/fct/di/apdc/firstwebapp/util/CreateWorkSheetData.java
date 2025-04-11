package pt.unl.fct.di.apdc.firstwebapp.util;

public class CreateWorkSheetData {
    public String authToken;
    
    // Atributos obrigatórios:
    public String reference;
    public String description;
    public String targetType;
    public String adjudicationStatus;
    
    // Atributos opcionais a preencher se adjudicada:
    public String adjudicationDate;
    public String expectedStartDate;
    public String expectedCompletionDate;
    public String partnerAccount;
    public String adjudicationEntity;
    public String companyNIF;
    public String workState;
    public String observations;
    
    public CreateWorkSheetData() { }
    
    public CreateWorkSheetData(String authToken, String reference, String description, String targetType,
            String adjudicationStatus) {
        this.authToken = authToken;
        this.reference = reference;
        this.description = description;
        this.targetType = targetType;
        this.adjudicationStatus = adjudicationStatus;
    }
}
