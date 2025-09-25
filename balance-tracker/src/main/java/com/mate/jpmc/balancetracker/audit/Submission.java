package com.mate.jpmc.balancetracker.audit;

import java.util.List;

public record Submission(List<Batch> batches) {
}
