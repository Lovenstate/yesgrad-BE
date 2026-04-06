package com.yesgrad.service.dto;

import java.util.List;

public record SubjectNode(
        Long id, String name, List<SubjectNode> children
        ) {
}
