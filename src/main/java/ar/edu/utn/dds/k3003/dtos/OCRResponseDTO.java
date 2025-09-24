package ar.edu.utn.dds.k3003.dtos;

import java.util.List;

public class OCRResponseDTO {
    private List<ParsedResult> ParsedResults;

    public List<ParsedResult> getParsedResults() {
        return ParsedResults;
    }

    public void setParsedResults(List<ParsedResult> parsedResults) {
        ParsedResults = parsedResults;
    }

    public static class ParsedResult {
        private String ParsedText;

        public String getParsedText() {
            return ParsedText;
        }

        public void setParsedText(String parsedText) {
            ParsedText = parsedText;
        }
    }
}