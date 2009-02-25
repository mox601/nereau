--
-- PostgreSQL database dump
--

SET client_encoding = 'LATIN9';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: classes; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE classes (
    id integer NOT NULL,
    idterm integer NOT NULL,
    idtag integer NOT NULL,
    iduser integer NOT NULL,
    value real NOT NULL
);


ALTER TABLE public.classes OWNER TO web81_nereau;

--
-- Name: cooccurrences; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE cooccurrences (
    id integer NOT NULL,
    idclass integer NOT NULL,
    idterm integer NOT NULL,
    value real NOT NULL
);


ALTER TABLE public.cooccurrences OWNER TO web81_nereau;

--
-- Name: stemmedterms; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE stemmedterms (
    id integer NOT NULL,
    stemmedterm text NOT NULL
);


ALTER TABLE public.stemmedterms OWNER TO web81_nereau;

--
-- Name: tags; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE tags (
    id integer NOT NULL,
    tag text NOT NULL
);


ALTER TABLE public.tags OWNER TO web81_nereau;

--
-- Name: terms; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE terms (
    id integer NOT NULL,
    idstemmedterm integer NOT NULL,
    term text NOT NULL,
    relevance integer DEFAULT 0
);


ALTER TABLE public.terms OWNER TO web81_nereau;

--
-- Name: users; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE users (
    id integer NOT NULL,
    username text NOT NULL,
    password text NOT NULL,
    lastupdate bigint,
    firstname text,
    lastname text,
    email text,
    role integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.users OWNER TO web81_nereau;

--
-- Name: visitedurls; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE visitedurls (
    id integer NOT NULL,
    iduser integer NOT NULL,
    url text NOT NULL,
    query text NOT NULL,
    expandedquery text,
    date bigint NOT NULL
);


ALTER TABLE public.visitedurls OWNER TO web81_nereau;

--
-- Name: visitedurltags; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE visitedurltags (
    id integer NOT NULL,
    idvisitedurl integer NOT NULL,
    idtag integer NOT NULL,
    value real NOT NULL
);


ALTER TABLE public.visitedurltags OWNER TO web81_nereau;

--
-- Name: votes; Type: TABLE; Schema: public; Owner: web81_nereau; Tablespace: 
--

CREATE TABLE votes (
    idvote integer NOT NULL,
    iduser integer NOT NULL,
    query text NOT NULL,
    expandedquery text NOT NULL,
    tags text NOT NULL
);


ALTER TABLE public.votes OWNER TO web81_nereau;

--
-- Name: classes_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE classes_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.classes_id_seq OWNER TO web81_nereau;

--
-- Name: classes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE classes_id_seq OWNED BY classes.id;


--
-- Name: cooccurrences_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE cooccurrences_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cooccurrences_id_seq OWNER TO web81_nereau;

--
-- Name: cooccurrences_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE cooccurrences_id_seq OWNED BY cooccurrences.id;


--
-- Name: stemmedterms_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE stemmedterms_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.stemmedterms_id_seq OWNER TO web81_nereau;

--
-- Name: stemmedterms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE stemmedterms_id_seq OWNED BY stemmedterms.id;


--
-- Name: tags_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE tags_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tags_id_seq OWNER TO web81_nereau;

--
-- Name: tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE tags_id_seq OWNED BY tags.id;


--
-- Name: terms_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE terms_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.terms_id_seq OWNER TO web81_nereau;

--
-- Name: terms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE terms_id_seq OWNED BY terms.id;


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE users_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO web81_nereau;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- Name: visitedurls_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE visitedurls_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.visitedurls_id_seq OWNER TO web81_nereau;

--
-- Name: visitedurls_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE visitedurls_id_seq OWNED BY visitedurls.id;


--
-- Name: visitedurltags_id_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE visitedurltags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.visitedurltags_id_seq OWNER TO web81_nereau;

--
-- Name: visitedurltags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE visitedurltags_id_seq OWNED BY visitedurltags.id;


--
-- Name: votes_idvote_seq; Type: SEQUENCE; Schema: public; Owner: web81_nereau
--

CREATE SEQUENCE votes_idvote_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.votes_idvote_seq OWNER TO web81_nereau;

--
-- Name: votes_idvote_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: web81_nereau
--

ALTER SEQUENCE votes_idvote_seq OWNED BY votes.idvote;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE classes ALTER COLUMN id SET DEFAULT nextval('classes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE cooccurrences ALTER COLUMN id SET DEFAULT nextval('cooccurrences_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE stemmedterms ALTER COLUMN id SET DEFAULT nextval('stemmedterms_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE tags ALTER COLUMN id SET DEFAULT nextval('tags_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE terms ALTER COLUMN id SET DEFAULT nextval('terms_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE visitedurls ALTER COLUMN id SET DEFAULT nextval('visitedurls_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE visitedurltags ALTER COLUMN id SET DEFAULT nextval('visitedurltags_id_seq'::regclass);


--
-- Name: idvote; Type: DEFAULT; Schema: public; Owner: web81_nereau
--

ALTER TABLE votes ALTER COLUMN idvote SET DEFAULT nextval('votes_idvote_seq'::regclass);


--
-- Name: classes_idterm_key; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY classes
    ADD CONSTRAINT classes_idterm_key UNIQUE (idterm, idtag, iduser);


--
-- Name: classes_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY classes
    ADD CONSTRAINT classes_pkey PRIMARY KEY (id);


--
-- Name: cooccurrences_idclass_key; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY cooccurrences
    ADD CONSTRAINT cooccurrences_idclass_key UNIQUE (idclass, idterm);


--
-- Name: cooccurrences_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY cooccurrences
    ADD CONSTRAINT cooccurrences_pkey PRIMARY KEY (id);


--
-- Name: stemmedterms_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY stemmedterms
    ADD CONSTRAINT stemmedterms_pkey PRIMARY KEY (id);


--
-- Name: stemmedterms_stemmedterm_key; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY stemmedterms
    ADD CONSTRAINT stemmedterms_stemmedterm_key UNIQUE (stemmedterm);


--
-- Name: tags_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: tags_tag_key; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY tags
    ADD CONSTRAINT tags_tag_key UNIQUE (tag);


--
-- Name: terms_idstemmedterm_key; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY terms
    ADD CONSTRAINT terms_idstemmedterm_key UNIQUE (idstemmedterm, term);


--
-- Name: terms_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY terms
    ADD CONSTRAINT terms_pkey PRIMARY KEY (id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users_username_key; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: visitedurls_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY visitedurls
    ADD CONSTRAINT visitedurls_pkey PRIMARY KEY (id);


--
-- Name: visitedurltags_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY visitedurltags
    ADD CONSTRAINT visitedurltags_pkey PRIMARY KEY (id);


--
-- Name: votes_pkey; Type: CONSTRAINT; Schema: public; Owner: web81_nereau; Tablespace: 
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_pkey PRIMARY KEY (idvote);


--
-- Name: classes_idtag_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY classes
    ADD CONSTRAINT classes_idtag_fkey FOREIGN KEY (idtag) REFERENCES tags(id);


--
-- Name: classes_idterm_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY classes
    ADD CONSTRAINT classes_idterm_fkey FOREIGN KEY (idterm) REFERENCES stemmedterms(id);


--
-- Name: classes_iduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY classes
    ADD CONSTRAINT classes_iduser_fkey FOREIGN KEY (iduser) REFERENCES users(id);


--
-- Name: cooccurrences_idclass_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY cooccurrences
    ADD CONSTRAINT cooccurrences_idclass_fkey FOREIGN KEY (idclass) REFERENCES classes(id);


--
-- Name: cooccurrences_idterm_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY cooccurrences
    ADD CONSTRAINT cooccurrences_idterm_fkey FOREIGN KEY (idterm) REFERENCES stemmedterms(id);


--
-- Name: terms_idstemmedterm_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY terms
    ADD CONSTRAINT terms_idstemmedterm_fkey FOREIGN KEY (idstemmedterm) REFERENCES stemmedterms(id);


--
-- Name: visitedurls_iduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY visitedurls
    ADD CONSTRAINT visitedurls_iduser_fkey FOREIGN KEY (iduser) REFERENCES users(id);


--
-- Name: visitedurltags_idtag_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY visitedurltags
    ADD CONSTRAINT visitedurltags_idtag_fkey FOREIGN KEY (idtag) REFERENCES tags(id);


--
-- Name: visitedurltags_idvisitedurl_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY visitedurltags
    ADD CONSTRAINT visitedurltags_idvisitedurl_fkey FOREIGN KEY (idvisitedurl) REFERENCES visitedurls(id);


--
-- Name: votes_iduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: web81_nereau
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_iduser_fkey FOREIGN KEY (iduser) REFERENCES users(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

