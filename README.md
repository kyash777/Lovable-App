flowchart LR

    U[User]

    U --> FE[React Frontend]

    FE --> GW[Spring Cloud Gateway]

    GW --> INT[Intelligence Service]
    GW --> WS[Workspace Service]
    GW --> CHAT[Chat Service]

    INT --> LLM[LLM Provider]

    INT --> QD[Qdrant Vector DB]
    INT --> MINIO[MinIO Object Storage]

    INT --> EXEC[Execution Service]

    EXEC --> K8S[Kubernetes Cluster]

    K8S --> PREVIEW[Preview URL]

    PREVIEW --> U
