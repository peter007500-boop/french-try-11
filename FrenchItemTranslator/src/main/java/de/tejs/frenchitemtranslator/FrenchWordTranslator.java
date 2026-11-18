package de.tejs.frenchitemtranslator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A deliberately literal glossary, not a grammar-aware or natural translation. */
public final class FrenchWordTranslator {
    private static final Pattern WORD = Pattern.compile("\\p{L}+(?:['’]\\p{L}+)*", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Map<String, String> WORDS = createGlossary();

    private FrenchWordTranslator() {}

    public static String translateLiterally(String french) {
        Matcher matcher = WORD.matcher(french);
        StringBuffer result = new StringBuffer();
        boolean translatedAtLeastOneWord = false;

        while (matcher.find()) {
            String original = matcher.group();
            String replacement = WORDS.get(original.toLowerCase(Locale.ROOT));
            if (replacement != null) {
                translatedAtLeastOneWord = true;
                replacement = matchCapitalization(original, replacement);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(original));
            }
        }
        matcher.appendTail(result);
        return translatedAtLeastOneWord ? result.toString() : null;
    }

    private static String matchCapitalization(String original, String translated) {
        if (!original.isEmpty() && Character.isUpperCase(original.codePointAt(0))) {
            int first = translated.codePointAt(0);
            return new String(Character.toChars(Character.toUpperCase(first)))
                    + translated.substring(Character.charCount(first));
        }
        return translated;
    }

    private static Map<String, String> createGlossary() {
        Map<String, String> words = new HashMap<>();
        add(words,
                // Articles, prepositions and connectors (kept literal on purpose)
                "de", "der", "du", "dem", "des", "der", "la", "die", "le", "der",
                "les", "die", "un", "ein", "une", "eine", "en", "aus", "à", "zu",
                "et", "und", "avec", "mit", "sans", "ohne", "pour", "für", "dans", "in",
                "sur", "auf", "noir", "schwarz", "noire", "schwarze", "rouge", "rot",
                "bleu", "blau", "verte", "grün", "vert", "grün", "blanche", "weiß", "blanc", "weiß",
                // Wood, trees and plants
                "bois", "Holz", "bûche", "Stamm", "bûches", "Stämme", "chêne", "Eiche",
                "bouleau", "Birke", "sapin", "Fichte", "épicéa", "Fichte", "acacia", "Akazie",
                "jungle", "Dschungel", "mangrove", "Mangrove", "cerisier", "Kirschbaum",
                "chêne", "Eiche", "pommier", "Apfelbaum", "pomme", "Apfel", "feuille", "Blatt",
                "feuilles", "Blätter", "fleur", "Blume", "fleurs", "Blumen", "herbe", "Gras",
                "blé", "Weizen", "graine", "Samen", "graines", "Samen", "champignon", "Pilz",
                // Building blocks and materials
                "planches", "Bretter", "planche", "Brett", "pierre", "Stein", "pavé", "Pflasterstein",
                "roche", "Fels", "terre", "Erde", "sable", "Sand", "gravier", "Kies", "argile", "Ton",
                "brique", "Ziegel", "briques", "Ziegel", "verre", "Glas", "laine", "Wolle",
                "béton", "Beton", "concrete", "Beton", "dalle", "Platte", "escaliers", "Treppen",
                "escalier", "Treppe", "mur", "Mauer", "clôture", "Zaun", "porte", "Tür",
                "trappe", "Falltür", "coffre", "Truhe", "baril", "Fass", "four", "Ofen",
                "table", "Tisch", "fabrication", "Herstellung", "établi", "Werkbank",
                // Ores, metals and gems
                "fer", "Eisen", "or", "Gold", "diamant", "Diamant", "diamants", "Diamanten",
                "émeraude", "Smaragd", "cuivre", "Kupfer", "charbon", "Kohle", "redstone", "Redstone",
                "lapis", "Lapis", "lazuli", "Lazuli", "netherite", "Netherit", "lingot", "Barren",
                "lingots", "Barren", "minerai", "Erz", "minerais", "Erze", "pépite", "Nugget",
                "pépites", "Nuggets", "brut", "roh", "brute", "rohe", "cristal", "Kristall",
                "améthyste", "Amethyst", "quartz", "Quarz", "obsidienne", "Obsidian",
                // Tools, weapons and armor
                "épée", "Schwert", "épées", "Schwerter", "pioche", "Spitzhacke", "pioches", "Spitzhacken",
                "hache", "Axt", "haches", "Äxte", "pelle", "Schaufel", "houe", "Hacke",
                "arc", "Bogen", "arbalète", "Armbrust", "flèche", "Pfeil", "flèches", "Pfeile",
                "bouclier", "Schild", "casque", "Helm", "plastron", "Brustplatte", "jambières", "Beinschutz",
                "bottes", "Stiefel", "armure", "Rüstung", "canne", "Angel", "pêche", "Fischen",
                "cisailles", "Schere", "briquet", "Feuerzeug", "seau", "Eimer", "seau", "Eimer",
                // Food and drops
                "pain", "Brot", "viande", "Fleisch", "crue", "roh", "cuit", "gekocht", "cuite", "gekocht",
                "poulet", "Huhn", "bœuf", "Rind", "porc", "Schwein", "mouton", "Schaf",
                "poisson", "Fisch", "saumon", "Lachs", "morue", "Kabeljau", "carotte", "Karotte",
                "patate", "Kartoffel", "pomme", "Apfel", "dorée", "goldene", "doré", "goldener",
                "sucre", "Zucker", "gâteau", "Kuchen", "lait", "Milch", "œuf", "Ei",
                "plume", "Feder", "cuir", "Leder", "os", "Knochen", "ficelle", "Faden",
                "poudre", "Pulver", "encre", "Tinte", "sac", "Beutel", "sacs", "Beutel",
                // Other common Minecraft vocabulary
                "bâton", "Stock", "bâtons", "Stöcke", "torche", "Fackel", "torches", "Fackeln",
                "échelle", "Leiter", "bateau", "Boot", "wagonnet", "Minecart", "rail", "Schiene",
                "cloche", "Glocke", "chaîne", "Kette", "lanterne", "Laterne", "bougie", "Kerze",
                "livre", "Buch", "livres", "Bücher", "papier", "Papier", "carte", "Karte",
                "boussole", "Kompass", "horloge", "Uhr", "lit", "Bett", "musique", "Musik",
                "disque", "Schallplatte", "œil", "Auge", "ender", "Ender", "perle", "Perle",
                "perles", "Perlen", "potion", "Trank", "pomme", "Apfel", "totem", "Totem",
                "étoile", "Stern", "feu", "Feuer", "charge", "Ladung", "explosif", "Sprengstoff",
                "fusée", "Rakete", "feux", "Feuer", "artifice", "Feuerwerk"
        );
        return Map.copyOf(words);
    }

    private static void add(Map<String, String> map, String... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
    }
}
