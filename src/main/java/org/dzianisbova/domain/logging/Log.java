package org.dzianisbova.domain.logging;

import java.time.Instant;

public sealed interface Log permits SuccessLog, ErrorLog {
    Instant creationTime();
}
