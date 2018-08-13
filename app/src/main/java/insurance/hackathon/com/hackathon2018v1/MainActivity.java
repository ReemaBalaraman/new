package insurance.hackathon.com.hackathon2018v1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private Button Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Email = (EditText)findViewById(R.id.Email);
        Password = (EditText)findViewById(R.id.Password);
        Login = (Button)findViewById(R.id.btnLogin);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCredentials(Email.getText().toString(),Password.getText().toString());
            }
        });

    }

    private void validateCredentials(String userEmail,String userPassword) {
        if(userEmail.equalsIgnoreCase("admin@abc.com") && userPassword.equals("a")) {
             Intent intent = new Intent(MainActivity.this, LandingPageActivity.class);
             startActivity(intent);
        }else {

        }
        }
    }


