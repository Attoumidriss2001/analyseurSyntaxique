import java.util.*;

public class AnalyseurSyntaxique {
    private Map<String, List<List<String>>> grammaire;
    private Map<String, Set<String>> premiers;
    private Map<String, Set<String>> suivants;
    private Map<String, Map<String, List<String>>> tableAnalyse;
    private static final String AXIOME = "E";


    public AnalyseurSyntaxique(Map<String, List<List<String>>> grammaire) {
        this.grammaire = grammaire;
        this.premiers = new HashMap<>();
        this.suivants = new HashMap<>();
        this.tableAnalyse = new HashMap<>();
    }

    public void calculerPremiers() {
        for (String nonTerminal : grammaire.keySet()) {
            calculerPremier(nonTerminal);
        }

    }

    private void calculerPremier(String nonTerminal) {
        if (!premiers.containsKey(nonTerminal)) {
            premiers.put(nonTerminal, new HashSet<>());
        }

        //get la production du non terminal et le stocke dans deux listes imbriqué
        List<List<String>> prod = grammaire.get(nonTerminal);
        //parcourir la premiere liste
        for (List<String> production : prod) {
            //pour chaque element  de la premier liste on traite le premier element
            String premierSymbole = production.get(0);
            //verification est ce que cet element est terminal ou non si oui on l'ajoute a la liste des premiers
            if (estTerminal(premierSymbole)) {
                premiers.get(nonTerminal).add(premierSymbole);
            } else {
                //la fonction recursive pour calculer le premier du non terminal
                calculerPremier(premierSymbole);
                //on determine le premier de premier symbole et on l'ajout dans la liste des premiers du non terminal
                Set<String> premierDePremierSymbole = premiers.get(premierSymbole);
                premiers.get(nonTerminal).addAll(premierDePremierSymbole);

                int i = 1;
                while (i < production.size() && premierDePremierSymbole.contains("ε")) {
                    premierSymbole = production.get(i);

                    if (estTerminal(premierSymbole)) {
                        premiers.get(nonTerminal).add(premierSymbole);
                        break;
                    }

                    calculerPremier(premierSymbole);

                    premierDePremierSymbole = premiers.get(premierSymbole);
                    premiers.get(nonTerminal).addAll(premierDePremierSymbole);

                    i++;
                }

                if (i == production.size() && premierDePremierSymbole.contains("ε")) {
                    premiers.get(nonTerminal).add("ε");
                }
            }
        }
    }


    public void calculerSuivants() {
        // Initialisation des ensembles de suivants pour chaque non-terminal
        for (String nonTerminal : grammaire.keySet()) {
            suivants.put(nonTerminal, new HashSet<>());
        }

        // Ajout du symbole de fin de chaîne ($) comme suivant du premier non-terminal
        suivants.get(grammaire.keySet().iterator().next()).add("$");

        boolean modifie;
        do {
            modifie = false;

            // Parcours de tous les non-terminaux de la grammaire
            for (String nonTerminal : grammaire.keySet()) {
                // Si c'est l'axiome, on ajoute le symbole de fin de chaîne ($) comme suivant
                if (nonTerminal.equals(AXIOME)) {
                    modifie |= suivants.get(nonTerminal).add("$");
                }

                // Parcours de toutes les productions de chaque non-terminal
                for (Map.Entry<String, List<List<String>>> entry : grammaire.entrySet()) {
                    String symboleGauche = entry.getKey();
                    List<List<String>> prod = entry.getValue();

                    for (List<String> production : prod) {
                        // Parcours de chaque symbole dans la production
                        for (int i = 0; i < production.size(); i++) {
                            String symbole = production.get(i);

                            // Si le symbole est égal au non-terminal actuel
                            if (symbole.equals(nonTerminal)) {
                                //verifier si le dernier element de la production
                                if (i == production.size() - 1) {
                                    if (!symboleGauche.equals(nonTerminal)) {
                                        Set<String> suivantDeSymboleGauche = suivants.get(symboleGauche);
                                        modifie |= suivants.get(nonTerminal).addAll(suivantDeSymboleGauche);
                                    }
                                } else {
                                    // Sinon, il y a une suite de symboles après le non-terminal actuel
                                    List<String> suite = production.subList(i + 1, production.size());
                                    Set<String> premiersDeSuite = calculerPremiersProduction(suite);

                                    if (premiersDeSuite.contains("ε")) {
                                        // Si la suite peut produire le symbole vide
                                        premiersDeSuite.remove("ε");
                                        modifie |= suivants.get(nonTerminal).addAll(premiersDeSuite);

                                        // Ajout des suivants du symbole de gauche (si différent du non-terminal actuel)
                                        if (!symboleGauche.equals(nonTerminal)) {
                                            Set<String> suivantDeSymboleGauche = suivants.get(symboleGauche);
                                            modifie |= suivants.get(nonTerminal).addAll(suivantDeSymboleGauche);
                                        }
                                    } else {
                                        // Sinon, ajout des premiers de la suite comme suivants
                                        modifie |= suivants.get(nonTerminal).addAll(premiersDeSuite);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } while (modifie);
    }

    private Set<String> calculerPremiersProduction(List<String> production) {
        Set<String> premiersProd = new HashSet<>();
        
        if (production.isEmpty()) {
            premiersProd.add("ε");
            return premiersProd;
        }
        //    Si la liste production n'est pas vide, la méthode récupère le premier
        //    symbole de la liste en utilisant production.get(0) et le stocke dans une variable appelée premierSymbole.
        String premierSymbole = production.get(0);
        //vérifie si ce premierSymbole est un symbole terminal.
        // Un symbole terminal est un symbole qui ne peut pas être développé davantage.
        //  Si premierSymbole est un symbole terminal, il est ajouté à l'ensemble premiersProd.
        if (estTerminal(premierSymbole)) {
            premiersProd.add(premierSymbole);
        }
        // Si premierSymbole n'est pas un symbole terminal, cela signifie qu'il est non terminal
        // et peut être développé davantage. La méthode récupère alors tous les premiers symboles
        // associés à ce premierSymbole à partir d'une structure de données appelée premiers

        else {
            premiersProd.addAll(premiers.get(premierSymbole));
            //  la méthode entre dans une boucle qui itère à partir du deuxième symbole (i = 1) jusqu'à la fin de
            //  la liste production. La boucle vérifie deux conditions : d'une part, si premierSymbole a
            //  la possibilité de produire la chaîne vide "ε" (c'est-à-dire si la liste des premiers symboles
            //  de premierSymbole contient "ε") et
            //  d'autre part, si la boucle n'a pas déjà rencontré un symbole terminal dans les symboles précédents de la production.
            int i = 1;

            while (i < production.size() && premiers.get(premierSymbole).contains("ε")) {
                // chaque itération de la boucle, la méthode récupère le symbole à l'index i
                // de la liste production et le stocke dans premierSymbole. Ensuite, elle
                // effectue
             
                // si premierSymbole est un symbole terminal, il est ajouté à premiersProd et la
                // boucle est interrompue.
                premierSymbole = production.get(i);
                // Si premierSymbole est un symbole non terminal, la méthode récupère tous les premiers symboles associés
                //  à premierSymbole à partir de la structure de données premiers et les ajoute à premiersProd.
                if (estTerminal(premierSymbole)) {
                    premiersProd.add(premierSymbole);
                    break;
                }
                premiersProd.addAll(premiers.get(premierSymbole));
                i++;
            }
            // si premierSymbole peut produire la chaîne vide "ε
            if (i == production.size() && premiers.get(premierSymbole).contains("ε")) {
                premiersProd.add("ε");
            }
        }

        return premiersProd;
    }

    private boolean estTerminal(String symbole) {
        return !Character.isUpperCase(symbole.charAt(0));
    }

    private List<String> getTerminaux() {
        Set<String> terminaux = new HashSet<>();

        for (List<List<String>> grammaireList : grammaire.values()) {
            for (List<String> production : grammaireList) {
                for (String symbole : production) {
                    if (estTerminal(symbole)) {
                        terminaux.add(symbole);
                    }
                }
            }
        }

        terminaux.remove("ε");
        terminaux.add("$");

        return new ArrayList<>(terminaux);
    }

    public boolean verifierAppartenance(String chaine) {
        System.out.println("\nLes éléments de la pile :");
        Stack<String> stack = new Stack<>();
        stack.push("$"); // Symbole de fin de pile
        stack.push("E"); // Symbole initial de la grammaire

        int index = 0;
        String a = String.valueOf(chaine.charAt(index)); // Premier symbole d'entrée

        while (true) {
            String x = stack.peek(); // Symbole au sommet de la pile

            // Afficher la valeur de la pile
            System.out.println("Pile: " + stack);

            // Condition de terminaison : les symboles de fin de pile et d'entrée sont tous les deux '$'
            if (x.equals("$") && a.equals("$")) {
                return true;
            }

            // Si le symbole au sommet de la pile correspond au symbole d'entrée courant
            if (x.equals(a) && !a.equals("$")) {
                stack.pop(); // Retirer le symbole de la pile

                index++;
                if (index < chaine.length()) {
                    a = String.valueOf(chaine.charAt(index)); // Passer au symbole d'entrée suivant
                } else {
                    a = "$"; // Si la chaîne d'entrée est terminée, a est défini comme '$'
                }

                continue;
            }

            // Si le symbole au sommet de la pile est une variable non terminale
            if (grammaire.containsKey(x)) {
                Map<String, List<String>> row = tableAnalyse.get(x);
                List<String> production = row.get(a);

                if (production != null) {
                    stack.pop(); // Retirer le symbole de la pile

                    // Effectuer la réduction en retirant les symboles de la production de la pile
                    for (int i = production.size() - 1; i >= 0; i--) {
                        String symbol = production.get(i);
                        if (!symbol.equals("ε")) { // Ignorer le symbole d'épsilon
                            stack.push(symbol); // Empiler les symboles de la production
                        }
                    }

                    continue;
                } else {
                    return false; // La production n'est pas définie dans la table d'analyse
                }
            }

            return false; // Aucune correspondance trouvée, la chaîne n'appartient pas à la grammaire
        }
    }



    public void construireTableAnalyse() {
        // Pour chaque symbole non terminal dans le dictionnaire des productions,
        //  on crée une entrée vide correspondante dans la table d'analyse.
        for (String nonTerminal : grammaire.keySet()) {
            tableAnalyse.put(nonTerminal, new HashMap<>());
        }

//     Pour chaque production dans la liste :
        // On calcule l'ensemble des premiers symboles de la production.
        // On obtient l'ensemble des symboles suivants pour le symbole non terminal actuel.
        // On parcourt les premiers symboles et, pour chaque symbole différent de "ε", on
        // ajoute la production correspondante à la table d'analyse pour le symbole non terminal.
        // Si "ε" est dans les premiers symboles, on parcourt les symboles suivants et on ajoute
        //  la production correspondante à la table d'analyse pour le symbole non terminal.
        for (Map.Entry<String, List<List<String>>> entry : grammaire.entrySet()) {
            String nonTerminal = entry.getKey();
            List<List<String>> prod = entry.getValue();

            for (List<String> production : prod) {
                Set<String> premiersProd = calculerPremiersProduction(production);
                Set<String> suivantsNonTerminal = suivants.get(nonTerminal);

                for (String symbole : premiersProd) {
                    if (!symbole.equals("ε")) {
                        tableAnalyse.get(nonTerminal).put(symbole, production);
                    }
                }

                if (premiersProd.contains("ε")) {
                    for (String symbole : suivantsNonTerminal) {
                        tableAnalyse.get(nonTerminal).put(symbole, production);
                    }
                }
            }
        }
    }


    //-------------------------------------------------------------------------------------------------------

    public void afficherPremiers() {
        System.out.println("Premiers :");
        for (Map.Entry<String, Set<String>> entry : premiers.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();
    }

    public void afficherSuivants() {
        System.out.println("Suivants :");
        for (Map.Entry<String, Set<String>> entry : suivants.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();
    }

    private void afficherTableAnalyse() {
        System.out.println("La table d'analyse :");
        List<String> terminaux = getTerminaux();

      
        System.out.print("+----------+");
        for (String terminal : terminaux) {
            System.out.print(formatCell(terminal));
        }
        System.out.println("+");


        for (String nonTerminal : grammaire.keySet()) {
            if (nonTerminal.equals("$")) {
                continue;
            }

            System.out.print("| " + formatCell(nonTerminal));
            for (String terminal : terminaux) {
                List<String> production = tableAnalyse.get(nonTerminal).get(terminal);
                String cellContent = production != null ? String.join(" ", production) : "";
                System.out.print(formatCell(cellContent));
            }
            System.out.println("|");
        }

        // Ligne de séparation
        System.out.print("+-----------------------------------------------------------------------+");

    }

    private void afficherRegle() {
        System.out.println(" \n\n Les regles :  ");
        List<String> terminaux = getTerminaux();

        for (String nonTerminal : grammaire.keySet()) {
            if (nonTerminal.equals("$")) {
                continue;
            }

            List<List<String>> prod = grammaire.get(nonTerminal);
            Set<String> productionSet = new LinkedHashSet<>();

            for (List<String> production : prod) {
                StringBuilder sb = new StringBuilder();
                for (String symbole : production) {
                    sb.append("\"").append(symbole).append("\", ");
                }
                String productionString = sb.toString().trim();
                if (!productionString.isEmpty()) {
                    productionString = productionString.substring(0, productionString.length() - 1);
                }
                productionSet.add(productionString);
            }

            System.out.print("\"" + nonTerminal + "\"  --> ");
            int count = 0;
            for (String production : productionSet) {
                count++;
                System.out.print(production);
                if (count < productionSet.size()) {
                    System.out.print(" | ");
                }
            }
            System.out.println();
        }
    }

    private String formatCell(String content) {
        return formatCell(content, " ");
    }

    private String formatCell(String content, String paddingChar) {
        int cellWidth = 7;
        String paddedContent = content + String.join("", Collections.nCopies(cellWidth - content.length(), paddingChar));
        return " " + paddedContent + " |";
    }

    //-------------------------------------------------------------------------------------------------------





    public static void main(String[] args) {
        Map<String, List<List<String>>> grammaire = new HashMap<>();
        grammaire.put("E", Arrays.asList(Arrays.asList("T", "E'")));
        grammaire.put("E'", Arrays.asList(Arrays.asList("+", "T", "E'"), Arrays.asList("ε")));
        grammaire.put("T", Arrays.asList(Arrays.asList("F", "T'")));
        grammaire.put("T'", Arrays.asList(Arrays.asList("*", "F", "T'"), Arrays.asList("ε")));
        grammaire.put("F", Arrays.asList(Arrays.asList("(", "E", ")"), Arrays.asList("n")));

        AnalyseurSyntaxique analyseur = new AnalyseurSyntaxique(grammaire);
        analyseur.calculerPremiers();
        analyseur.calculerSuivants();
        analyseur.construireTableAnalyse();

        analyseur.afficherPremiers();
        analyseur.afficherSuivants();
        analyseur.afficherTableAnalyse();
        analyseur.afficherRegle();

        String chaine = "n+n*n";
        boolean appartient = analyseur.verifierAppartenance(chaine);

        if (appartient) {
            System.out.println(" cette chaîne \"" + chaine + "\" est engendrée par la grammaire du langage.");
        } else {
            System.out.println("cette chaîne \"" + chaine + "\" n'est pas engendrée par la grammaire du langage.");
        }
    }
}