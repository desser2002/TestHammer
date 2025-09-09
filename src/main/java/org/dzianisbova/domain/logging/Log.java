package org.dzianisbova.domain.logging;

public sealed interface Log permits SuccessLog, ErrorLog {
}
