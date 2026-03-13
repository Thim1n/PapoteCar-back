FROM mysql:8.0

ENV MYSQL_DATABASE=papotecar \
    MYSQL_USER=papotecar_user \
    MYSQL_PASSWORD=papotecar_pass \
    MYSQL_ROOT_PASSWORD=root_secret


COPY initdb/ /docker-entrypoint-initdb.d/

EXPOSE 3306
