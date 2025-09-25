package ar.edu.utn.dds.k3003.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OCRResponseDTO {

    @JsonProperty("ParsedResults")
    private List<ParsedResult> parsedResults;

    @JsonProperty("OCRExitCode")
    private Integer ocrExitCode;

    @JsonProperty("IsErroredOnProcessing")
    private Boolean isErroredOnProcessing;

    @JsonProperty("ErrorMessage")
    private List<String> errorMessage;

    @JsonProperty("ProcessingTimeInMilliseconds")
    private String processingTimeInMilliseconds;

    @JsonProperty("SearchablePDFURL")
    private String searchablePDFURL;

    public List<ParsedResult> getParsedResults() {
        return parsedResults;
    }

    public Integer getOcrExitCode() {
        return ocrExitCode;
    }

    public Boolean getIsErroredOnProcessing() {
        return isErroredOnProcessing;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public String getProcessingTimeInMilliseconds() {
        return processingTimeInMilliseconds;
    }

    public String getSearchablePDFURL() {
        return searchablePDFURL;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedResult {

        @JsonProperty("ParsedText")
        private String parsedText;

        @JsonProperty("TextOverlay")
        private Object textOverlay;

        @JsonProperty("TextOrientation")
        private String textOrientation;

        @JsonProperty("FileParseExitCode")
        private Integer fileParseExitCode;

        @JsonProperty("ErrorMessage")
        private String errorMessage;

        @JsonProperty("ErrorDetails")
        private String errorDetails;

        public String getParsedText() {
            return parsedText;
        }

        public String getTextOrientation() {
            return textOrientation;
        }

        public Integer getFileParseExitCode() {
            return fileParseExitCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorDetails() {
            return errorDetails;
        }
    }
}
