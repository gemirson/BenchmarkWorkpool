# Workpool - Sistema de Gerenciamento de Threads em Java

## Visão Geral
O Workpool é uma implementação Java para gerenciamento eficiente de threads, oferecendo duas abordagens distintas:
1. **SimpleWorkpool**: Implementação leve e de alto desempenho
2. **EventLoopWorkpool**: Implementação alternativa para casos específicos

## Funcionalidades Principais
- Gerenciamento automatizado de threads
- Balanceamento de carga inteligente
- Mecanismos de retry para tarefas
- Monitoramento de desempenho integrado
- Configuração flexível via WorkpoolBuilder

## Estrutura do Projeto
```
src/
├── main/
│   ├── java/com/example/workpool/
│   │   ├── SimpleWorkpool.java      # Implementação principal
│   │   ├── EventLoopWorkpool.java   # Implementação alternativa  
│   │   ├── WorkpoolFactory.java     # Factory pattern
│   │   └── WorkpoolBuilder.java     # Builder pattern
├── test/
│   ├── java/com/example/workpool/
│   │   ├── WorkpoolBenchmarkTest.java # Testes de performance
│   │   └── WorkpoolStressTest.java    # Testes de carga
```

## Como Usar

### Configuração Básica
```java
Workpool workpool = new SimpleWorkpool(
    Runtime.getRuntime().availableProcessors(), // Número de threads
    10_000 // Tamanho da fila
);
```

### Submetendo Tarefas
```java
workpool.submitTask(() -> {
    // Sua lógica aqui
});
```

### Desempenho
Consulte o arquivo [performance.me](performance.me) para métricas detalhadas.

| Métrica          | SimpleWorkpool | EventLoopWorkpool |
|------------------|----------------|-------------------|
| Throughput       | 1.68M TPS      | 155K TPS          |
| Uso de Memória   | 2 MB           | 1039 MB           |

## Requisitos
- Java 21+
- Maven 3.6+

## Instalação
```bash
mvn clean install
```

## Contribuição
1. Faça um fork do projeto
2. Crie sua feature branch (`git checkout -b feature/fooBar`)
3. Commit suas mudanças (`git commit -am 'Add some fooBar'`)
4. Push para a branch (`git push origin feature/fooBar`)
5. Crie um novo Pull Request

## Licença
MIT © 2025 Workpool Contributors
