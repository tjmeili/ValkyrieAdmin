package com.meilinger.tj.admin;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by TJ on 3/28/2018.
 */

public class AddEmployeeDialogFragment extends DialogFragment {

    public AddEmployeeDialogFragment() {
    }

    public interface AddEmployeeListener{
        void addEmployee(String firstName, String lastName);
    }

    public static AddEmployeeDialogFragment newInstance(){
        return new AddEmployeeDialogFragment();
    }

    public AddEmployeeListener delegate = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Dialog_NoActionBar );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_add_employee, container, false);
        final EditText etFirstName = v.findViewById(R.id.etFirstName);
        final EditText etLastName = v.findViewById(R.id.etLastName);
        Button okButton = v.findViewById(R.id.okButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = etFirstName.getText().toString();
                String lastName = etLastName.getText().toString();
                if(!firstName.isEmpty() && !lastName.isEmpty()){
                    if(delegate != null){
                        delegate.addEmployee(etFirstName.getText().toString(), etLastName.getText().toString());
                    }
                }
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        etFirstName.requestFocus();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddEmployeeListener) {
            delegate = (AddEmployeeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EmployeeSelectListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        delegate = null;
    }
}
