package com.project.asset.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationError {
    String field;
    String message;
}


