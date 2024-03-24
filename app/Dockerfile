FROM openjdk:21-slim-bookworm

RUN apt-get update && \
    apt-get install -y \
      wget && \
    rm -rf /var/lib/apt/lists/*
RUN wget -O /usr/local/bin/yt-dlp \
    https://github.com/yt-dlp/yt-dlp/releases/download/2024.03.10/yt-dlp_linux && \
    chmod a+x /usr/local/bin/yt-dlp
RUN adduser --system --group derezzed
USER derezzed

ADD --chown=derezzed:derezzed ./build/install/derezzed/ /opt/derezzed/

CMD /opt/derezzed/bin/derezzed
