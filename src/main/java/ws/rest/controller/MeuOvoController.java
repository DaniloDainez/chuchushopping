package ws.rest.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ws.helper.OntologyHelper;
import ws.rest.response.SpaceResponse;
import ws.service.SpaceService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MeuOvoController {

    private final SpaceService spaceService;

    public MeuOvoController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping("/sparql")
    public String sparqlQuery(@RequestParam String query) {
        return OntologyHelper.sparql(query);
    }

    @GetMapping("/lojas")
    public String getStores() {
        String response = OntologyHelper.sparql("SELECT ?loja WHERE {[a :Store; rdfs:label ?loja]}");
        System.out.println(response);
        return response;
    }

    @GetMapping("/estrutura")
    public ResponseEntity<List<SpaceResponse>> getStructure() {
        return ResponseEntity.of(Optional.of(spaceService.getAllStores()));
    }

    @GetMapping("/loja/{label}")
    public String getStoreByLabel(@PathVariable("label") String label) {
        // ia fazer o bagulho pra evitar SparQL injection
        // https://morelab.deusto.es/code_injection/files/sparql_injection.pdf
        // mas a gente ta liberando endpoint de sparql entao dane-se

        return OntologyHelper.sparql(String.format("SELECT ?nome ?telefone ?website ?produtos\n" +
                "WHERE {\n" +
                "    [a :Store;\n" +
                "        rdfs:label \"%s\";\n" +
                "        :website ?website;\n" +
                "        :telephone ?telefone]\n" +
                "}", label));
    }

    // fica meio pt/en pq o front fica mais bonito se usar os endpoint BR,
    // mas no cod vai nas gringa senao o cabeca chora
    @GetMapping("/produtos")
    public String productsSoldBy(@RequestParam(required = false, value = "loja") String storeLabel) {
        if(StringUtils.isNotBlank(storeLabel)) {
            return OntologyHelper.sparql(String.format(
                    "SELECT ?productLabel ?price ?category\n" +
                            "WHERE {\n" +
                            "    [a :Store ;\n" +
                            "        rdfs:label \"%s\" ;\n" +
                            "        :sells [a :Product;\n" +
                            "        rdfs:label ?productLabel ;\n" +
                            "        :price ?price ;\n" +
                            "        :hasCategory [a :Category ; rdfs:label ?category]]]\n" +
                            "}", storeLabel
            ));
        }
        return OntologyHelper.sparql("SELECT ?productLabel ?price ?category\n" +
                "WHERE {\n" +
                "\t[a :Product ;\n" +
                "\t\trdfs:label ?productLabel ;\n" +
                "\t\t:price ?price;\n" +
                "\t\t:hasCategory [a :Category ; rdfs:label ?category]]\n" +
                "}");
    }
}