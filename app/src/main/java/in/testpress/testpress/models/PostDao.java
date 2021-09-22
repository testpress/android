package in.testpress.testpress.models;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;

import in.testpress.testpress.models.Post;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "POST".
*/
public class PostDao extends AbstractDao<Post, Long> {

    public static final String TABLENAME = "POST";

    /**
     * Properties of entity Post.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property Title = new Property(1, String.class, "title", false, "TITLE");
        public final static Property Summary = new Property(2, String.class, "summary", false, "SUMMARY");
        public final static Property ContentHtml = new Property(3, String.class, "contentHtml", false, "CONTENT_HTML");
        public final static Property Url = new Property(4, String.class, "url", false, "URL");
        public final static Property PublishedDate = new Property(5, String.class, "publishedDate", false, "PUBLISHED_DATE");
        public final static Property Published = new Property(6, Long.class, "published", false, "PUBLISHED");
        public final static Property Modified = new Property(7, String.class, "modified", false, "MODIFIED");
        public final static Property Institute = new Property(8, Integer.class, "institute", false, "INSTITUTE");
        public final static Property Is_active = new Property(9, Boolean.class, "is_active", false, "IS_ACTIVE");
        public final static Property ModifiedDate = new Property(10, Long.class, "modifiedDate", false, "MODIFIED_DATE");
        public final static Property Short_web_url = new Property(11, String.class, "short_web_url", false, "SHORT_WEB_URL");
        public final static Property Short_url = new Property(12, String.class, "short_url", false, "SHORT_URL");
        public final static Property ShortLink = new Property(13, String.class, "shortLink", false, "SHORT_LINK");
        public final static Property Web_url = new Property(14, String.class, "web_url", false, "WEB_URL");
        public final static Property CommentsCount = new Property(15, Integer.class, "commentsCount", false, "COMMENTS_COUNT");
        public final static Property CommentsUrl = new Property(16, String.class, "commentsUrl", false, "COMMENTS_URL");
        public final static Property CoverImage = new Property(17, String.class, "coverImage", false, "COVER_IMAGE");
        public final static Property CoverImageMedium = new Property(18, String.class, "coverImageMedium", false, "COVER_IMAGE_MEDIUM");
        public final static Property CoverImageSmall = new Property(19, String.class, "coverImageSmall", false, "COVER_IMAGE_SMALL");
        public final static Property Slug = new Property(20, String.class, "slug", false, "SLUG");
        public final static Property CategoryId = new Property(21, Long.class, "categoryId", false, "CATEGORY_ID");
    };

    private DaoSession daoSession;


    public PostDao(DaoConfig config) {
        super(config);
    }
    
    public PostDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"POST\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"TITLE\" TEXT," + // 1: title
                "\"SUMMARY\" TEXT," + // 2: summary
                "\"CONTENT_HTML\" TEXT," + // 3: contentHtml
                "\"URL\" TEXT," + // 4: url
                "\"PUBLISHED_DATE\" TEXT," + // 5: publishedDate
                "\"PUBLISHED\" INTEGER," + // 6: published
                "\"MODIFIED\" TEXT," + // 7: modified
                "\"INSTITUTE\" INTEGER," + // 8: institute
                "\"IS_ACTIVE\" INTEGER," + // 9: is_active
                "\"MODIFIED_DATE\" INTEGER," + // 10: modifiedDate
                "\"SHORT_WEB_URL\" TEXT," + // 11: short_web_url
                "\"SHORT_URL\" TEXT," + // 12: short_url
                "\"SHORT_LINK\" TEXT," + // 13: shortLink
                "\"WEB_URL\" TEXT," + // 14: web_url
                "\"COMMENTS_COUNT\" INTEGER," + // 15: commentsCount
                "\"COMMENTS_URL\" TEXT," + // 16: commentsUrl
                "\"COVER_IMAGE\" TEXT," + // 17: coverImage
                "\"COVER_IMAGE_MEDIUM\" TEXT," + // 18: coverImageMedium
                "\"COVER_IMAGE_SMALL\" TEXT," + // 19: coverImageSmall
                "\"SLUG\" TEXT," + // 20: slug
                "\"CATEGORY_ID\" INTEGER);"); // 21: categoryId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"POST\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Post entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
 
        String summary = entity.getSummary();
        if (summary != null) {
            stmt.bindString(3, summary);
        }
 
        String contentHtml = entity.getContentHtml();
        if (contentHtml != null) {
            stmt.bindString(4, contentHtml);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }
 
        String publishedDate = entity.getPublishedDate();
        if (publishedDate != null) {
            stmt.bindString(6, publishedDate);
        }
 
        Long published = entity.getPublished();
        if (published != null) {
            stmt.bindLong(7, published);
        }
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(8, modified);
        }
 
        Integer institute = entity.getInstitute();
        if (institute != null) {
            stmt.bindLong(9, institute);
        }
 
        Boolean is_active = entity.getIs_active();
        if (is_active != null) {
            stmt.bindLong(10, is_active ? 1L: 0L);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(11, modifiedDate);
        }
 
        String short_web_url = entity.getShort_web_url();
        if (short_web_url != null) {
            stmt.bindString(12, short_web_url);
        }
 
        String short_url = entity.getShort_url();
        if (short_url != null) {
            stmt.bindString(13, short_url);
        }
 
        String shortLink = entity.getShortLink();
        if (shortLink != null) {
            stmt.bindString(14, shortLink);
        }
 
        String web_url = entity.getWeb_url();
        if (web_url != null) {
            stmt.bindString(15, web_url);
        }
 
        Integer commentsCount = entity.getCommentsCount();
        if (commentsCount != null) {
            stmt.bindLong(16, commentsCount);
        }
 
        String commentsUrl = entity.getCommentsUrl();
        if (commentsUrl != null) {
            stmt.bindString(17, commentsUrl);
        }
 
        String coverImage = entity.getCoverImage();
        if (coverImage != null) {
            stmt.bindString(18, coverImage);
        }
 
        String coverImageMedium = entity.getCoverImageMedium();
        if (coverImageMedium != null) {
            stmt.bindString(19, coverImageMedium);
        }
 
        String coverImageSmall = entity.getCoverImageSmall();
        if (coverImageSmall != null) {
            stmt.bindString(20, coverImageSmall);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(21, slug);
        }
 
        Long categoryId = entity.getCategoryId();
        if (categoryId != null) {
            stmt.bindLong(22, categoryId);
        }
    }

    @Override
    protected void attachEntity(Post entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Post readEntity(Cursor cursor, int offset) {
        Post entity = new Post( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // title
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // summary
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // contentHtml
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // url
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // publishedDate
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6), // published
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // modified
            cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8), // institute
            cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0, // is_active
            cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10), // modifiedDate
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // short_web_url
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // short_url
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // shortLink
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // web_url
            cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15), // commentsCount
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // commentsUrl
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // coverImage
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18), // coverImageMedium
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19), // coverImageSmall
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // slug
            cursor.isNull(offset + 21) ? null : cursor.getLong(offset + 21) // categoryId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Post entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSummary(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setContentHtml(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setPublishedDate(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPublished(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
        entity.setModified(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setInstitute(cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8));
        entity.setIs_active(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
        entity.setModifiedDate(cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10));
        entity.setShort_web_url(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setShort_url(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setShortLink(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setWeb_url(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setCommentsCount(cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15));
        entity.setCommentsUrl(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setCoverImage(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setCoverImageMedium(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setCoverImageSmall(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setSlug(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setCategoryId(cursor.isNull(offset + 21) ? null : cursor.getLong(offset + 21));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Post entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Post entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getCategoryDao().getAllColumns());
            builder.append(" FROM POST T");
            builder.append(" LEFT JOIN CATEGORY T0 ON T.\"CATEGORY_ID\"=T0.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Post loadCurrentDeep(Cursor cursor, boolean lock) {
        Post entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Category category = loadCurrentOther(daoSession.getCategoryDao(), cursor, offset);
        entity.setCategory(category);

        return entity;    
    }

    public Post loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Post> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Post> list = new ArrayList<Post>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Post> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Post> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
