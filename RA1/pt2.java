import java.io.*;

public class pt2 {

    static final int CLAU = 8; // desplaçament del xifrat Cèsar

    public static void main(String[] args) {
        xifrar("entrada.txt", "xifrat.txt", CLAU);
        desxifrar("xifrat.txt", "desxifrat.txt", CLAU);
    }

    // Inverteix el contingut d'una línia (String -> String al revés)
    private static String invertirLinia(String linia) {
        return new StringBuilder(linia).reverse().toString();
    }

    // Desplaça cada caràcter de la línia N posicions en Unicode
    private static String desplacar(String linia, int desplacament) {
        StringBuilder resultat = new StringBuilder();

        for (int i = 0; i < linia.length(); i++) {
            char c = linia.charAt(i);
            char nou = (char) (c + desplacament);
            resultat.append(nou);
        }

        return resultat.toString();
    }

    public static void xifrar(String fitxerEntrada, String fitxerSortida, int clau) {
        System.out.println("Iniciant xifrat de '" + fitxerEntrada + "'...");

        try (
            BufferedReader br = new BufferedReader(new FileReader(fitxerEntrada));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fitxerSortida))
        ) {
            String linia;
            int numLinies = 0;

            while ((linia = br.readLine()) != null) {
                String invertida = invertirLinia(linia);
                String xifrada = desplacar(invertida, clau);

                bw.write(xifrada);
                bw.newLine();

                numLinies++;
            }

            System.out.println("Xifrat complet: " + numLinies + " línies escrites a '" + fitxerSortida + "'.");

        } catch (FileNotFoundException e) {
            System.out.println("Error: el fitxer '" + fitxerEntrada + "' no existeix.");
        } catch (IOException e) {
            System.out.println("Error d'entrada/sortida durant el xifrat: " + e.getMessage());
        }
    }

    public static void desxifrar(String fitxerXifrat, String fitxerSortida, int clau) {
        System.out.println("Iniciant desxifrat de '" + fitxerXifrat + "'...");

        try (
            BufferedReader br = new BufferedReader(new FileReader(fitxerXifrat));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fitxerSortida))
        ) {
            String linia;
            int numLinies = 0;

            while ((linia = br.readLine()) != null) {
                String desplacada = desplacar(linia, -clau); // desfem el desplaçament
                String original = invertirLinia(desplacada);  // desfem la inversió

                bw.write(original);
                bw.newLine();

                numLinies++;
            }

            System.out.println("Desxifrat complet: " + numLinies + " línies escrites a '" + fitxerSortida + "'.");

        } catch (FileNotFoundException e) {
            System.out.println("Error: el fitxer '" + fitxerXifrat + "' no existeix.");
        } catch (IOException e) {
            System.out.println("Error d'entrada/sortida durant el desxifrat: " + e.getMessage());
        }
    }
}
