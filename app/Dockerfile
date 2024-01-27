FROM openjdk:21-slim-bookworm

RUN apt-get update && \
    apt-get install -y \
      wget && \
    rm -rf /var/lib/apt/lists/*

RUN adduser --system --group derezzed
USER derezzed

ADD --chown=derezzed:derezzed ./build/install/derezzed/ /opt/derezzed/

CMD /opt/derezzed/bin/derezzed
