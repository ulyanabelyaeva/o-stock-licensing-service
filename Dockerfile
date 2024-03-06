# Базовый образ, содержащий среду Java времени выполнения
FROM openjdk:17-slim
# Добавить информацию о владельце
LABEL maintainer="Belyaeva Ulyana <ulyanabelyaeva02@gmail.com>"
# Файл jar приложения
ARG JAR_FILE
# Добавить файл jar приложения в контейнер
COPY ${JAR_FILE} app.jar
# запустить приложение
ENTRYPOINT ["java","-jar","/app.jar"]