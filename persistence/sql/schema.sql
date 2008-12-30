CREATE DATABASE nereau;

CREATE TABLE users
(
	id serial NOT NULL,
	username text NOT NULL UNIQUE,
	password text NOT NULL,
	
	PRIMARY KEY (id)
);

CREATE TABLE stemmedterms
(
	id serial NOT NULL,
	stemmedterm text NOT NULL UNIQUE,

	PRIMARY KEY (id)
);

CREATE TABLE terms
(
	id serial NOT NULL,
	idstemmedterm integer NOT NULL,
	term text NOT NULL,
	relevance integer DEFAULT 0,

	PRIMARY KEY (id),
	UNIQUE(idstemmedterm,term),
	FOREIGN KEY(idstemmedterm) 
		REFERENCES stemmedterms(id)
);

CREATE TABLE tags
(
	id serial NOT NULL,
	tag text NOT NULL UNIQUE,
	
	PRIMARY KEY (id)
);

CREATE TABLE visitedurls
(
	id serial NOT NULL,
	iduser integer NOT NULL,
	url text NOT NULL,
	query text NOT NULL,
	date bigint NOT NULL,

	PRIMARY KEY (id),
	FOREIGN KEY(iduser)
		REFERENCES users(id)
);

CREATE TABLE classes
(
	id serial NOT NULL,
	idterm integer NOT NULL,
	idtag integer NOT NULL,
	iduser integer NOT NULL,
	value real NOT NULL,

	PRIMARY KEY (id),
	UNIQUE(idterm,idtag,iduser),
	FOREIGN KEY(idterm) 
		REFERENCES stemmedterms(id),
	FOREIGN KEY(idtag) 
		REFERENCES tags(id),
	FOREIGN KEY(iduser) 
		REFERENCES users(id)
);

CREATE TABLE cooccurrences
(
	id serial NOT NULL,
	idclass integer NOT NULL,
	idterm integer NOT NULL,
	value real NOT NULL,

	PRIMARY KEY (id),
	UNIQUE(idclass,idterm),
	FOREIGN KEY(idclass) 
		REFERENCES classes(id),
	FOREIGN KEY(idterm) 
		REFERENCES stemmedterms(id)
);
