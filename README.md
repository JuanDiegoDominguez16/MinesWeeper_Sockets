# MinesWeeper Sockets

Buscaminas multijugador por sockets TCP. Varios clientes juegan sobre el mismo
tablero alojado en el servidor.

## Requisitos

- Un JDK 21 o superior.
- Maven **no** hace falta instalarlo: el proyecto trae el Maven Wrapper
  (`mvnw` / `mvnw.cmd`), que se descarga Maven solo la primera vez.

Si `java -version` no muestra 21+, apunta `JAVA_HOME` al JDK antes de nada:

```powershell
setx JAVA_HOME "C:\ruta\a\tu\jdk-21"
```

(hay que abrir una terminal nueva para que surta efecto)

## Ejecutar

Abre **dos terminales** en la raiz del proyecto.

Servidor:

```powershell
.\mvnw.cmd compile exec:java -Pserver
```

Cliente (puedes abrir varios, todos juegan en el mismo tablero):

```powershell
.\mvnw.cmd compile exec:java -Pclient
```

En Linux/macOS es lo mismo con `./mvnw`.

## Otros comandos

| Comando | Que hace |
|---|---|
| `.\mvnw.cmd compile` | Solo compila |
| `.\mvnw.cmd package` | Genera `target/minesweeper-sockets-1.0-SNAPSHOT.jar` |
| `.\mvnw.cmd clean` | Borra `target/` |

El jar generado arranca el servidor directamente:

```powershell
java -jar target\minesweeper-sockets-1.0-SNAPSHOT.jar
```

Desde VS Code tambien puedes usar F5 con las configuraciones
**Minesweeper Server** y **Minesweeper Client**.

## Estructura

```
pom.xml
src/main/java/minesweeper/
    client/      Client            cliente de consola
    server/      Server            acepta conexiones
                 HandlerClient     un hilo por cliente
    game/        Board             tablero compartido
                 BoardObserver     notifica cambios
    connection/  Message, Action   lo que viaja por el socket
                 MessageType, ActionType
```
