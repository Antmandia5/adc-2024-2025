package pt.unl.fct.di.apdc.firstwebapp.util;

public class CreateWorkSheetData {
    // Token de autenticação (JSON)
    public String authToken;
    
    // Atributos obrigatórios:
    public String reference;         // Ex.: "O234/CM/2024"
    public String description;       // Ex.: "Obra para reflorestação da propriedade P34567..."
    public String targetType;        // Ex.: "Propriedade Pública" ou "Propriedade Privada"
    public String adjudicationStatus;  // Ex.: "ADJUDICADO" ou "NÃO ADJUDICADO"
    
    // Atributos opcionais a preencher se adjudicada:
    public String adjudicationDate;         // Data de adjudicação (ex.: "2024-03-15")
    public String expectedStartDate;        // Data prevista de início
    public String expectedCompletionDate;   // Data prevista de conclusão
    public String partnerAccount;           // Conta de entidade (com role PARTNER)
    public String adjudicationEntity;       // Nome da Empresa
    public String companyNIF;               // NIF da empresa
    public String workState;                // Ex.: "NÃO INICIADO", "EM CURSO" ou "CONCLUÍDO"
    public String observations;             // Observações sobre a obra
    
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
