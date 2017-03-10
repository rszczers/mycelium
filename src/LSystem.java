public class LSystem {
    String[] rules;
    String alphabet;
    String axiom;
    Interpretation interp;
//    HashMap<Character, ?> interpretation;

    public LSystem(String[] rules, String alphabet, String axiom, Interpretation interp) {
        for (int i = 0; i < rules.length; i++) {

        }
        this.rules = rules;
        this.alphabet = alphabet;
        this.axiom = axiom;
        this.interp = interp;
    }

    public String run(int steps) {
        for (int i = 0; i < steps; i++) {
            char c = axiom.charAt(i);
            if (c == 'F') {

            } else if (c == 'F') {

            } else if (c == '+') {

            } else if (c == '-') {

            } else if (c == '[') {

            } else if (c == ']') {

            }
        }
        return null;
    }

    private String rewrite() {
        return null;
    }

    private String iterate(String steps, String rule) {
        String newProduction = this.axiom;
        String[] prod = rule.split("->");

        newProduction = newProduction.replaceAll(prod[0], prod[1]);
        return newProduction;
    }
}
